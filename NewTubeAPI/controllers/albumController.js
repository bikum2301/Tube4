const Album = require('../models/Album'); // Import Album model
const mongoose = require('mongoose');

// @desc    Get all albums
// @route   GET /albums
// @access  Public
exports.getAllAlbums = async (req, res, next) => {
  try {
    const albums = await Album.find({}); // Lấy tất cả albums
    // TODO: Add pagination
    res.status(200).json({
      success: true,
      count: albums.length,
      data: albums
    });
  } catch (error) {
    console.error("Get All Albums Error:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi lấy danh sách album' });
  }
};

// @desc    Get single album by ID
// @route   GET /albums/:id
// @access  Public
exports.getAlbumById = async (req, res, next) => {
  try {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
        return res.status(400).json({ success: false, message: 'ID album không hợp lệ' });
    }
    const album = await Album.findById(req.params.id); //.populate('songs'); // Nếu bạn muốn lấy cả danh sách bài hát thuộc album

    if (!album) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy album' });
    }
    res.status(200).json({ success: true, data: album });
  } catch (error) {
    console.error("Get Album By ID Error:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi lấy thông tin album' });
  }
};

// @desc    Create a new album
// @route   POST /albums
// @access  Private (Nên bảo vệ sau)
exports.createAlbum = async (req, res, next) => {
  try {
    const newAlbum = await Album.create(req.body); // Dữ liệu từ client (title, artist, albumArtPath...)
    res.status(201).json({ success: true, data: newAlbum });
  } catch (error) {
    console.error("Create Album Error:", error);
    if (error.name === 'ValidationError') {
       const messages = Object.values(error.errors).map(val => val.message);
       return res.status(400).json({ success: false, message: 'Dữ liệu không hợp lệ', errors: messages });
    } else if (error.code === 11000) { // Lỗi unique index (ví dụ trùng title + artist)
       return res.status(400).json({ success: false, message: 'Album này có thể đã tồn tại' });
    }
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi tạo album' });
  }
};

// @desc    Update an album by ID
// @route   PUT /albums/:id
// @access  Private (Nên bảo vệ sau)
exports.updateAlbum = async (req, res, next) => {
  try {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
        return res.status(400).json({ success: false, message: 'ID album không hợp lệ' });
    }
    let album = await Album.findById(req.params.id);
    if (!album) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy album để cập nhật' });
    }
    // TODO: Check ownership if needed

    album = await Album.findByIdAndUpdate(req.params.id, req.body, {
      new: true,
      runValidators: true
    });
    res.status(200).json({ success: true, data: album });
  } catch (error) {
    console.error("Update Album Error:", error);
    if (error.name === 'ValidationError') {
       const messages = Object.values(error.errors).map(val => val.message);
       return res.status(400).json({ success: false, message: 'Dữ liệu cập nhật không hợp lệ', errors: messages });
    } else if (error.code === 11000) {
       return res.status(400).json({ success: false, message: 'Thông tin cập nhật có thể gây trùng lặp album' });
    }
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi cập nhật album' });
  }
};

// @desc    Delete an album by ID
// @route   DELETE /albums/:id
// @access  Private (Nên bảo vệ sau)
exports.deleteAlbum = async (req, res, next) => {
  try {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
        return res.status(400).json({ success: false, message: 'ID album không hợp lệ' });
    }
    const album = await Album.findById(req.params.id);
    if (!album) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy album để xóa' });
    }
    // TODO: Check ownership if needed
    // TODO: Consider what happens to songs associated with this album

    await album.deleteOne();
    res.status(200).json({ success: true, message: 'Album đã được xóa', data: {} });
  } catch (error) {
    console.error("Delete Album Error:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi xóa album' });
  }
};