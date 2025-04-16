const express = require('express');
const {
  getAllArtists,
  getArtistById,
  createArtist,
  updateArtist,
  deleteArtist
} = require('../controllers/artistController'); // Import từ artistController

const router = express.Router();

router.route('/')
  .get(getAllArtists)
  .post(createArtist); // TODO: Add auth middleware

router.route('/:id')
  .get(getArtistById)
  .put(updateArtist)   // TODO: Add auth middleware
  .delete(deleteArtist); // TODO: Add auth middleware

module.exports = router;