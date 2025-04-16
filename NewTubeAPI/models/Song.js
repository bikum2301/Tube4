const mongoose = require('mongoose');

const songSchema = new mongoose.Schema({
  title: { type: String, required: true, trim: true },
  artist: { type: String, default: 'Unknown Artist', trim: true }, // Có thể tham chiếu đến Artist model sau
  album: { type: String, default: 'Unknown Album', trim: true },   // Có thể tham chiếu đến Album model sau
  duration: { type: Number, required: true }, // Lưu bằng milliseconds
  filePath: { type: String, required: true }, // Đường dẫn trên server hoặc URL cloud
  albumArtPath: { type: String }, // Đường dẫn ảnh bìa
  // Thêm các trường khác nếu cần: genre, year, playCount,...
}, { timestamps: true });

module.exports = mongoose.model('Song', songSchema);