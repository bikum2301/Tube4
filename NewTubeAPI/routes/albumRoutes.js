const express = require('express');
const {
  getAllAlbums,
  getAlbumById,
  createAlbum,
  updateAlbum,
  deleteAlbum
} = require('../controllers/albumController'); // Import từ albumController

const router = express.Router();

router.route('/')
  .get(getAllAlbums)
  .post(createAlbum); // TODO: Add auth middleware

router.route('/:id')
  .get(getAlbumById)
  .put(updateAlbum)   // TODO: Add auth middleware
  .delete(deleteAlbum); // TODO: Add auth middleware

module.exports = router;