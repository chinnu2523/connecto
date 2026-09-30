type EventListener = (event: any) => void;

class WebSocketClient {
  private ws: WebSocket | null = null;
  private subscribedChannels: Set<string> = new Set();
  private listeners: Set<EventListener> = new Set();
  private pingInterval: any = null;

  public connect() {
    if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
      return;
    }

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/api/v1/ws`;

    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      // Re-subscribe to all channels automatically on connection or re-connection!
      if (this.subscribedChannels.size > 0) {
        this.send({
          type: 'subscribe',
          channels: Array.from(this.subscribedChannels),
        });
      }

      // Heartbeat ping every 25s
      this.pingInterval = setInterval(() => {
        if (this.ws?.readyState === WebSocket.OPEN) {
          this.send({ type: 'ping' });
        }
      }, 25000);
    };

    this.ws.onmessage = (event) => {
      try {
        const parsed = JSON.parse(event.data);
        this.listeners.forEach((listener) => listener(parsed));
      } catch {
        // Ignore parsing errors
      }
    };

    this.ws.onclose = () => {
      if (this.pingInterval) clearInterval(this.pingInterval);

      // Reconnect after 3s
      setTimeout(() => {
        this.connect();
      }, 3000);
    };

    this.ws.onerror = () => {
      if (this.ws) this.ws.close();
    };
  }

  public subscribeChannel(channelId: string, channelName?: string) {
    if (channelId) this.subscribedChannels.add(channelId);
    if (channelName) this.subscribedChannels.add(channelName);
    const toSub = [channelId, channelName].filter(Boolean) as string[];
    if (this.ws && this.ws.readyState === WebSocket.OPEN && toSub.length > 0) {
      this.send({
        type: 'subscribe',
        channels: toSub,
      });
    }
  }

  public addListener(listener: EventListener) {
    this.listeners.add(listener);
    return () => {
      this.listeners.delete(listener);
    };
  }

  public send(data: any) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data));
    }
  }
}

export const wsClient = new WebSocketClient();
