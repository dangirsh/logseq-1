import { Capacitor } from '@capacitor/core'

export interface TaskNotificationPlugin {
  enableNotifications(options?: { intervalMinutes?: number }): Promise<void>
  disableNotifications(): Promise<void>
  checkNow(): Promise<void>
  isEnabled(): Promise<{ enabled: boolean }>
}

class TaskNotificationWeb implements TaskNotificationPlugin {
  async enableNotifications(): Promise<void> {
    console.warn('Task notifications are not available on web')
  }
  
  async disableNotifications(): Promise<void> {
    console.warn('Task notifications are not available on web')
  }
  
  async checkNow(): Promise<void> {
    console.warn('Task notifications are not available on web')
  }
  
  async isEnabled(): Promise<{ enabled: boolean }> {
    return { enabled: false }
  }
}

const TaskNotification = Capacitor.isPluginAvailable('TaskNotification')
  ? (Capacitor as any).Plugins.TaskNotification as TaskNotificationPlugin
  : new TaskNotificationWeb()

export default TaskNotification
