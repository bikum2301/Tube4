const mongoose = require('mongoose');

const albumSchema = new mongoose.Schema({
  title: { type: String, required: true, trim: true },
  artist: { type: String, default: 'Unknown Artist', trim: true }, // Tham chiếu Artist sau
  albumArtPath: { type: String },
  // songs: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Song' }], // Có thể thêm sau
  // year: { type: Number }
}, { timestamps: true });

// Đảm bảo title và artist là duy nhất (tránh trùng lặp album)
albumSchema.index({ title: 1, artist: 1 }, { unique: true });

module.exports = mongoose.model('Album', albumSchema);