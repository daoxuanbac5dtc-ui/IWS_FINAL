<template>
  <div class="chatbot-container">
    <!-- Chat Toggle Button -->
    <button 
      class="chat-toggle-btn" 
      @click="toggleChat"
      :class="{ 'has-notification': hasNewMessage }"
    >
      <svg v-if="!isChatOpen" viewBox="0 0 24 24" fill="currentColor">
        <path d="M20 2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h4l4 4 4-4h4c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/>
      </svg>
      <svg v-else viewBox="0 0 24 24" fill="currentColor">
        <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
      </svg>
      <div v-if="hasNewMessage" class="notification-dot"></div>
    </button>

    <!-- Chat Window -->
    <div class="chat-window" :class="{ 'open': isChatOpen }">
      <!-- Chat Header -->
      <div class="chat-header">
        <div class="header-info">
          <div class="avatar">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
          </div>
          <div>
            <h4>Hỗ trợ khách hàng</h4>
            <span class="status">{{ isTyping ? 'Đang nhập...' : 'Trực tuyến' }}</span>
          </div>
        </div>
        <button @click="toggleChat" class="close-btn">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
          </svg>
        </button>
      </div>

      <!-- Chat Messages -->
      <div class="chat-messages" ref="chatMessages">
        <div 
          v-for="message in messages" 
          :key="message.id"
          class="message"
          :class="{ 'user': message.type === 'user', 'bot': message.type === 'bot' }"
        >
          <div class="message-content">
            <p>{{ message.text }}</p>
            <div v-if="message.quickReplies" class="quick-replies">
              <button 
                v-for="reply in message.quickReplies"
                :key="reply"
                @click="sendQuickReply(reply)"
                class="quick-reply-btn"
              >
                {{ reply }}
              </button>
            </div>
          </div>
          <span class="message-time">{{ formatTime(message.timestamp) }}</span>
        </div>
        
        <!-- Typing Indicator -->
        <div v-if="isTyping" class="message bot">
          <div class="typing-indicator">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="quick-actions" v-if="!hasStartedChat">
        <button @click="sendQuickReply('Hỏi về sản phẩm')" class="action-btn">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
          </svg>
          Hỏi về sản phẩm
        </button>
        <button @click="sendQuickReply('Chính sách đổi trả')" class="action-btn">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          Chính sách đổi trả
        </button>
        <button @click="sendQuickReply('Thông tin giao hàng')" class="action-btn">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M5 8a1 1 0 100 2h5.586l-1.293 1.293a1 1 0 001.414 1.414L15 8.414V15a1 1 0 11-2 0v-4.586l-1.293 1.293a1 1 0 01-1.414-1.414L15 5.414 19.707 10.121a1 1 0 11-1.414 1.414L15 8.828v6.172a3 3 0 11-6 0V9a1 1 0 00-1-1H5z"/>
          </svg>
          Thông tin giao hàng
        </button>
      </div>

      <!-- Chat Input -->
      <div class="chat-input">
        <div class="input-wrapper">
          <input 
            v-model="currentMessage"
            @keypress.enter="sendMessage"
            placeholder="Nhập tin nhắn..."
            :disabled="isTyping"
          />
          <button @click="sendMessage" :disabled="!currentMessage.trim() || isTyping">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatBot',
  data() {
    return {
      isChatOpen: false,
      currentMessage: '',
      isTyping: false,
      hasNewMessage: false,
      hasStartedChat: false,
      messages: [
        {
          id: 1,
          type: 'bot',
          text: 'Xin chào! Tôi có thể giúp gì cho bạn hôm nay? 😊',
          timestamp: new Date(),
          quickReplies: ['Hỏi về sản phẩm', 'Chính sách đổi trả', 'Thông tin giao hàng']
        }
      ],
      // Bot responses database
      botResponses: {
        'hỏi về sản phẩm': {
          text: 'Bạn muốn biết thông tin gì về sản phẩm? Tôi có thể giúp bạn về: chất liệu, size, màu sắc, giá cả...',
          quickReplies: ['Hướng dẫn chọn size', 'Chất liệu sản phẩm', 'Giá cả']
        },
        'chính sách đổi trả': {
          text: 'Chúng tôi hỗ trợ đổi trả trong 7 ngày với điều kiện:\n• Sản phẩm còn nguyên tem, nhãn\n• Chưa qua sử dụng\n• Có hóa đơn mua hàng\n\nPhí ship đổi trả: 30.000đ',
          quickReplies: ['Cách thức đổi trả', 'Thời gian xử lý']
        },
        'thông tin giao hàng': {
          text: 'Thông tin giao hàng:\n• Nội thành: 1-2 ngày\n• Ngoại thành: 3-5 ngày\n• Freeship cho đơn từ 500k\n• COD toàn quốc',
          quickReplies: ['Kiểm tra đơn hàng', 'Thay đổi địa chỉ']
        },
        'hướng dẫn chọn size': {
          text: 'Để chọn size phù hợp:\n• Đo chân buổi chiều (chân to nhất)\n• Tham khảo bảng size chi tiết\n• Nên chọn size lớn hơn 0.5cm so với chân\n\nBạn muốn tôi gửi bảng size chi tiết không?',
          quickReplies: ['Gửi bảng size', 'Tư vấn size cụ thể']
        },
        'default': {
          text: 'Cảm ơn bạn đã liên hệ! Tôi sẽ chuyển cho nhân viên hỗ trợ. Hoặc bạn có thể gọi hotline: 0973.711.868',
          quickReplies: ['Gọi hotline', 'Để lại tin nhắn']
        }
      }
    }
  },
  
  methods: {
    toggleChat() {
      this.isChatOpen = !this.isChatOpen;
      if (this.isChatOpen) {
        this.hasNewMessage = false;
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      }
    },
    
    sendMessage() {
      if (!this.currentMessage.trim()) return;
      
      // Add user message
      const userMessage = {
        id: Date.now(),
        type: 'user',
        text: this.currentMessage,
        timestamp: new Date()
      };
      
      this.messages.push(userMessage);
      this.hasStartedChat = true;
      
      const messageText = this.currentMessage.toLowerCase();
      this.currentMessage = '';
      
      // Show typing and respond
      this.showTypingAndRespond(messageText);
    },
    
    sendQuickReply(reply) {
      this.currentMessage = reply;
      this.sendMessage();
    },
    
    showTypingAndRespond(messageText) {
      this.isTyping = true;
      this.scrollToBottom();
      
      setTimeout(() => {
        this.isTyping = false;
        this.generateBotResponse(messageText);
      }, 1000 + Math.random() * 1000); // Random delay 1-2s
    },
    
    generateBotResponse(messageText) {
      let response = this.botResponses['default'];
      
      // Simple keyword matching
      for (const [key, value] of Object.entries(this.botResponses)) {
        if (messageText.includes(key.toLowerCase())) {
          response = value;
          break;
        }
      }
      
      // Special cases
      if (messageText.includes('size') || messageText.includes('kích cỡ')) {
        response = this.botResponses['hướng dẫn chọn size'];
      } else if (messageText.includes('giá') || messageText.includes('bao nhiêu')) {
        response = {
          text: 'Giá sản phẩm hiện tại đã được hiển thị trên trang. Chúng tôi thường có các chương trình khuyến mãi. Bạn có muốn đăng ký nhận thông báo ưu đãi không?',
          quickReplies: ['Đăng ký ưu đãi', 'Xem sản phẩm khác']
        };
      }
      
      const botMessage = {
        id: Date.now(),
        type: 'bot',
        text: response.text,
        timestamp: new Date(),
        quickReplies: response.quickReplies
      };
      
      this.messages.push(botMessage);
      
      if (!this.isChatOpen) {
        this.hasNewMessage = true;
      }
      
      this.$nextTick(() => {
        this.scrollToBottom();
      });
    },
    
    scrollToBottom() {
      const container = this.$refs.chatMessages;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    },
    
    formatTime(date) {
      return new Intl.DateTimeFormat('vi-VN', {
        hour: '2-digit',
        minute: '2-digit'
      }).format(date);
    }
  }
}
</script>

<style scoped>

.avatar {
  background-image: url('/path-to-your-avatar.png');
  background-size: cover;
}
.chatbot-container {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  z-index: 1000;
}

.chat-toggle-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6452, #ff8a80);
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 25px rgba(255, 100, 82, 0.4);
  transition: all 0.3s ease;
  position: relative;
}

.chat-toggle-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(255, 100, 82, 0.5);
}

.chat-toggle-btn svg {
  width: 24px;
  height: 24px;
}

.chat-toggle-btn.has-notification {
  animation: pulse 2s infinite;
}

.notification-dot {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 12px;
  height: 12px;
  background: #ff4444;
  border-radius: 50%;
  border: 2px solid white;
}

.chat-window {
  position: absolute;
  bottom: 80px;
  right: 0;
  width: 380px;
  height: 500px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  opacity: 0;
  visibility: hidden;
  transform: translateY(20px) scale(0.9);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-window.open {
  opacity: 1;
  visibility: visible;
  transform: translateY(0) scale(1);
}

.chat-header {
  background: linear-gradient(135deg, #FF6452, #ff8a80);
  color: white;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.avatar {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar svg {
  width: 20px;
  height: 20px;
}

.header-info h4 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0;
}

.status {
  font-size: 0.8rem;
  opacity: 0.9;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.close-btn svg {
  width: 20px;
  height: 20px;
}

.chat-messages {
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  background: #f8fafc;
}

.message {
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease;
}

.message.user {
  align-items: flex-end;
}

.message.bot {
  align-items: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 0.75rem 1rem;
  border-radius: 18px;
  position: relative;
}

.message.user .message-content {
  background: linear-gradient(135deg, #FF6452, #ff8a80);
  color: white;
  border-bottom-right-radius: 6px;
}

.message.bot .message-content {
  background: white;
  color: #374151;
  border-bottom-left-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-content p {
  margin: 0;
  white-space: pre-line;
  line-height: 1.4;
}

.message-time {
  font-size: 0.7rem;
  color: #94a3b8;
  margin-top: 0.25rem;
  align-self: flex-end;
}

.message.bot .message-time {
  align-self: flex-start;
}

.quick-replies {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.quick-reply-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 0.5rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
}

.message.bot .quick-reply-btn {
  background: #f1f5f9;
  border-color: #e2e8f0;
  color: #64748b;
}

.quick-reply-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

.message.bot .quick-reply-btn:hover {
  background: #e2e8f0;
  color: #374151;
}

.typing-indicator {
  display: flex;
  gap: 0.25rem;
  padding: 1rem;
  background: white;
  border-radius: 18px;
  border-bottom-left-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #94a3b8;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

.quick-actions {
  padding: 1rem;
  background: white;
  border-top: 1px solid #e2e8f0;
}

.action-btn {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  margin-bottom: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
  font-size: 0.9rem;
  color: #374151;
}

.action-btn:hover {
  background: #f1f5f9;
  border-color: #FF6452;
  color: #FF6452;
}

.action-btn svg {
  width: 16px;
  height: 16px;
  color: #FF6452;
}

.chat-input {
  padding: 1rem;
  background: white;
  border-top: 1px solid #e2e8f0;
}

.input-wrapper {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.input-wrapper input {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 25px;
  outline: none;
  font-size: 0.9rem;
  transition: border-color 0.2s;
}

.input-wrapper input:focus {
  border-color: #FF6452;
}

.input-wrapper button {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #FF6452, #ff8a80);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.input-wrapper button:hover:not(:disabled) {
  transform: scale(1.05);
}

.input-wrapper button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.input-wrapper button svg {
  width: 18px;
  height: 18px;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

/* Responsive */
@media (max-width: 480px) {
  .chatbot-container {
    bottom: 1rem;
    right: 1rem;
  }
  
  .chat-window {
    width: calc(100vw - 2rem);
    height: 70vh;
    bottom: 70px;
    right: 0;
  }
}
</style>