const mongoose = require('mongoose');

const artistSchema = new mongoose.Schema({
  name: { type: String, required: true, unique: true, trim: true },
  // albums: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Album' }], // Có thể thêm sau
  // artistImagePath: { type: String }
}, { timestamps: true });

module.exports = mongoose.model('Artist', artistSchema);