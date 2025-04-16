const express = require('express');
const {
  getAllPlaylists,
  getPlaylistById,
  createPlaylist,
  updatePlaylist,
  deletePlaylist,
  addSongToPlaylist,
  removeSongFromPlaylist
} = require('../controllers/playlistController'); // Import từ playlistController

const router = express.Router();

// --- Routes cho Playlist ---
router.route('/')
  .get(getAllPlaylists)
  .post(createPlaylist); // TODO: Add auth middleware

router.route('/:id')
  .get(getPlaylistById)
  .put(updatePlaylist)   // TODO: Add auth middleware
  .delete(deletePlaylist); // TODO: Add auth middleware

// --- Routes để quản lý bài hát TRONG Playlist ---
router.route('/:playlistId/songs')
    .post(addSongToPlaylist); // TODO: Add auth middleware (thêm bài hát vào playlist)

router.route('/:playlistId/songs/:songId')
    .delete(removeSongFromPlaylist); // TODO: Add auth middleware (xóa bài hát khỏi playlist)


module.exports = router;