const Artist = require('../models/Artist'); // Import Artist model
const mongoose = require('mongoose');

// @desc    Get all artists
// @route   GET /artists
// @access  Public
exports.getAllArtists = async (req, res, next) => {
  try {
    const artists = await Artist.find({});
    // TODO: Add pagination
    res.status(200).json({
      success: true,
      count: artists.length,
      data: artists
    });
  } catch (error) {
    console.error("Get All Artists Error:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi lấy danh sách nghệ sĩ' });
  }
};

// @desc    Get single artist by ID
// @route   GET /artists/:id
// @access  Public
exports.getArtistById = async (req, res, next) => {
  try {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
        return res.status(400).json({ success: false, message: 'ID nghệ sĩ không hợp lệ' });
    }
    const artist = await Artist.findById(req.params.id); //.populate('albums'); // Nếu muốn lấy cả album

    if (!artist) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy nghệ sĩ' });
    }
    res.status(200).json({ success: true, data: artist });
  } catch (error) {
    console.error("Get Artist By ID Error:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi lấy thông tin nghệ sĩ' });
  }
};

// @desc    Create a new artist
// @route   POST /artists
// @access  Private (Nên bảo vệ sau)
exports.createArtist = async (req, res, next) => {
  try {
    // Client cần gửi 'name'
    const newArtist = await Artist.create(req.body);
    res.status(201).json({ success: true, data: newArtist });
  } catch (error) {
    console.error("Create Artist Error:", error);
    if (error.name === 'ValidationError') {
       const messages = Object.values(error.errors).map(val => val.message);
       return res.status(400).json({ success: false, message: 'Dữ liệu không hợp lệ', errors: messages });
    } else if (error.code === 11000) { // Lỗi unique 'name'
       return res.status(400).json({ success: false, message: 'Tên nghệ sĩ đã tồn tại' });
    }
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi tạo nghệ sĩ' });
  }
};

// @desc    Update an artist by ID
// @route   PUT /artists/:id
// @access  Private (Nên bảo vệ sau)
exports.updateArtist = async (req, res, next) => {
  try {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
        return res.status(400).json({ success: false, message: 'ID nghệ sĩ không hợp lệ' });
    }
    let artist = await Artist.findById(req.params.id);
    if (!artist) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy nghệ sĩ để cập nhật' });
    }
    // TODO: Check ownership if needed

    artist = await Artist.findByIdAndUpdate(req.params.id, req.body, {
      new: true,
      runValidators: true
    });
    res.status(200).json({ success: true, data: artist });
  } catch (error) {
    console.error("Update Artist Error:", error);
    if (error.name === 'ValidationError') {
       const messages = Object.values(error.errors).map(val => val.message);
       return res.status(400).json({ success: false, message: 'Dữ liệu cập nhật không hợp lệ', errors: messages });
    } else if (error.code === 11000) {
       return res.status(400).json({ success: false, message: 'Tên nghệ sĩ cập nhật đã tồn tại' });
    }
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi cập nhật nghệ sĩ' });
  }
};

// @desc    Delete an artist by ID
// @route   DELETE /artists/:id
// @access  Private (Nên bảo vệ sau)
exports.deleteArtist = async (req, res, next) => {
  try {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
        return res.status(400).json({ success: false, message: 'ID nghệ sĩ không hợp lệ' });
    }
    const artist = await Artist.findById(req.params.id);
    if (!artist) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy nghệ sĩ để xóa' });
    }
    // TODO: Check ownership if needed
    // TODO: Consider what happens to songs/albums associated with this artist

    await artist.deleteOne();
    res.status(200).json({ success: true, message: 'Nghệ sĩ đã được xóa', data: {} });
  } catch (error) {
    console.error("Delete Artist Error:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi xóa nghệ sĩ' });
  }
};