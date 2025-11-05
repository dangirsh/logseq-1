(ns frontend.mobile.components.task-notifications
  (:require [rum.core :as rum]
            [frontend.ui :as ui]
            [frontend.state :as state]
            [frontend.handler.notification :as notification]
            [promesa.core :as p]))

(defonce *notifications-enabled? (atom false))
(defonce *check-interval (atom 15))

(defn- enable-notifications []
  (p/let [_ (js/window.TaskNotification.enableNotifications 
              (clj->js {:intervalMinutes @*check-interval}))]
    (reset! *notifications-enabled? true)
    (notification/show! "Task notifications enabled" :success))
  (p/catch 
    (fn [e]
      (notification/show! (str "Failed to enable notifications: " (.-message e)) :error))))

(defn- disable-notifications []
  (p/let [_ (js/window.TaskNotification.disableNotifications)]
    (reset! *notifications-enabled? false)
    (notification/show! "Task notifications disabled" :success))
  (p/catch
    (fn [e]
      (notification/show! (str "Failed to disable notifications: " (.-message e)) :error))))

(defn- check-now []
  (p/let [_ (js/window.TaskNotification.checkNow)]
    (notification/show! "Checking for due tasks..." :success))
  (p/catch
    (fn [e]
      (notification/show! (str "Failed to check tasks: " (.-message e)) :error))))

(defn- load-status []
  (when js/window.TaskNotification
    (p/let [result (js/window.TaskNotification.isEnabled)]
      (reset! *notifications-enabled? (.-enabled result)))
    (p/catch
      (fn [e]
        (js/console.error "Failed to load notification status" e)))))

(rum/defc task-notification-settings < rum/reactive
  {:did-mount (fn [state]
                (load-status)
                state)}
  []
  (let [enabled? (rum/react *notifications-enabled?)
        interval (rum/react *check-interval)]
    [:div.task-notification-settings
     [:div.sm:flex.sm:items-start
      [:div.w-full
       [:div.text-sm.mb-4
        "Enable system notifications for tasks with SCHEDULED or DEADLINE timestamps. "
        "Similar to Orgzly's notification feature."]
       
       [:div.flex.items-center.justify-between.mb-4
        [:label.font-medium "Task Notifications"]
        (ui/toggle enabled?
                   (fn []
                     (if enabled?
                       (disable-notifications)
                       (enable-notifications)))
                   true)]
       
       (when enabled?
         [:div.mt-4.space-y-4
          [:div
           [:label.block.text-sm.font-medium.mb-2 
            (str "Check Interval: " interval " minutes")]
           [:input.form-range
            {:type "range"
             :min "5"
             :max "60"
             :step "5"
             :value interval
             :on-change (fn [e]
                          (let [new-val (js/parseInt (.. e -target -value))]
                            (reset! *check-interval new-val)
                            (when enabled?
                              (enable-notifications))))}]]
          
          [:div
           [:button.button
            {:on-click check-now}
            "Check for due tasks now"]]])]]]))

(defn init []
  (load-status))
