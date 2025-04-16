const mongoose = require('mongoose');

const playlistSchema = new mongoose.Schema({
  name: { type: String, required: true, trim: true },
  creator: { type: String, default: 'System' }, // Hoặc: { type: mongoose.Schema.Types.ObjectId, ref: 'User' }
  description: { type: String, trim: true },
  songs: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Song' }], // Mảng các ID của Song
  thumbnailUrl: { type: String }, // Ảnh đại diện playlist
}, { timestamps: true });

module.exports = mongoose.model('Playlist', playlistSchema);