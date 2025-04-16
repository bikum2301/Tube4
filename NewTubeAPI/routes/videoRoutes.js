const express = require('express');
const {
  getAllVideos,
  getVideoById,
  createVideo,
  updateVideo,
  deleteVideo,
  streamVideo // Import hàm stream
} = require('../controllers/videoController'); // Import từ videoController

const router = express.Router();

// --- Routes cho CRUD Video ---
router.route('/')
  .get(getAllVideos)
  .post(createVideo); // TODO: Add auth middleware

router.route('/:id')
  .get(getVideoById)
  .put(updateVideo)   // TODO: Add auth middleware
  .delete(deleteVideo); // TODO: Add auth middleware

// --- Route cho Streaming Video ---
router.route('/:id/stream')
  .get(streamVideo);    // GET /videos/:id/stream -> Stream video

module.exports = router;