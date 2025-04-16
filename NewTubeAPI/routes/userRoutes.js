const express = require('express');
const { registerUser, loginUser } = require('../controllers/userController');
const router = express.Router();

router.post('/register', registerUser);
router.post('/login', loginUser);
// Thêm các route khác cho user (profile, update,...) nếu cần

module.exports = router;