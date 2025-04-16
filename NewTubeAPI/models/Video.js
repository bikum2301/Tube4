const mongoose = require('mongoose');

const videoSchema = new mongoose.Schema({
  title: {
    type: String,
    required: [true, 'Vui lòng nhập tiêu đề video'],
    trim: true,
  },
  description: {
    type: String,
    trim: true,
  },
  duration: {
    type: Number, // Thời lượng tính bằng mili giây hoặc giây (giữ nhất quán)
    required: [true, 'Vui lòng nhập thời lượng video'],
  },
  filePath: {
    type: String,
    required: [true, 'Vui lòng cung cấp đường dẫn file video (URL hoặc local path)'],
  },
  thumbnailPath: {
    type: String, // Đường dẫn ảnh thumbnail (URL hoặc local path)
  },
  uploader: { // Tham chiếu đến người dùng đã upload (nếu có hệ thống user)
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    // required: true // Bỏ comment nếu yêu cầu phải có người upload
  },
  views: {
    type: Number,
    default: 0,
  },
  createdAt: {
    type: Date,
    default: Date.now,
  },
  // Thêm các trường khác nếu cần: tags, category, privacy (public/private)...
});

module.exports = mongoose.model('Video', videoSchema);