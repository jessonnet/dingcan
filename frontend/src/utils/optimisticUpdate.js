export class OptimisticUpdateManager {
  constructor() {
    this.history = []
    this.maxHistorySize = 10
  }

  saveState(data) {
    this.history.push({
      timestamp: Date.now(),
      data: JSON.parse(JSON.stringify(data))
    })

    if (this.history.length > this.maxHistorySize) {
      this.history.shift()
    }
  }

  restoreState() {
    if (this.history.length === 0) {
      return null
    }
    return this.history.pop().data
  }

  clearHistory() {
    this.history = []
  }

  getHistorySize() {
    return this.history.length
  }

  peekLastState() {
    if (this.history.length === 0) {
      return null
    }
    return this.history[this.history.length - 1].data
  }
}

export default OptimisticUpdateManager
