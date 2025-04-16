const express = require('express');
const {
  getAllSongs,
  getSongById,
  createSong,
  updateSong,
  deleteSong,
  streamSong // Import thêm streamSong
} = require('../controllers/songController'); // Import đầy đủ các hàm

const router = express.Router();

// --- Định nghĩa route cho /songs ---
router.route('/')
  .get(getAllSongs)   // GET /songs -> Lấy tất cả bài hát
  .post(createSong);  // POST /songs -> Tạo bài hát mới // TODO: Add auth middleware

// --- Định nghĩa route cho /songs/:id ---
router.route('/:id')
  .get(getSongById)    // GET /songs/:id -> Lấy chi tiết một bài hát
  .put(updateSong)     // PUT /songs/:id -> Cập nhật bài hát // TODO: Add auth middleware
  .delete(deleteSong); // DELETE /songs/:id -> Xóa bài hát // TODO: Add auth middleware

// --- Định nghĩa route cho streaming ---
router.route('/:id/stream')
  .get(streamSong);     // GET /songs/:id/stream -> Stream bài hát

module.exports = router;