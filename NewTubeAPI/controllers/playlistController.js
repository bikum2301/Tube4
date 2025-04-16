const Playlist = require('../models/Playlist'); // Import Playlist model
const Song = require('../models/Song'); // Cần để kiểm tra songId tồn tại
const mongoose = require('mongoose');

// @desc    Get all playlists (có thể là của user hoặc public)
// @route   GET /playlists
// @access  Public/Private
exports.getAllPlaylists = async (req, res, next) => {
  try {
    // TODO: Lọc playlist theo user nếu đã đăng nhập (req.user.id) hoặc chỉ lấy playlist public
    const playlists = await Playlist.find({}); //.populate('creator', 'username email'); // Lấy thông tin người tạo
    res.status(200).json({
      success: true,
      count: playlists.length,
      data: playlists
    });
  } catch (error) {
    console.error("Get All Playlists Error:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi lấy danh sách playlist' });
  }
};

// @desc    Get single playlist by ID
// @route   GET /playlists/:id
// @access  Public/Private
exports.getPlaylistById = async (req, res, next) => {
  try {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
        return res.status(400).json({ success: false, message: 'ID playlist không hợp lệ' });
    }
    // Populate 'songs' để lấy thông tin chi tiết các bài hát trong playlist
    const playlist = await Playlist.findById(req.params.id).populate('songs');
                                                    //.populate('creator', 'username email');

    if (!playlist) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy playlist' });
    }
    // TODO: Kiểm tra quyền xem playlist nếu là private

    res.status(200).json({ success: true, data: playlist });
  } catch (error) {
    console.error("Get Playlist By ID Error:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi lấy thông tin playlist' });
  }
};

// @desc    Create a new playlist
// @route   POST /playlists
// @access  Private (Cần user đăng nhập)
exports.createPlaylist = async (req, res, next) => {
  try {
    // TODO: Lấy userId từ req.user (sau khi có auth middleware) để gán vào creator
    // req.body.creator = req.user.id;

    // Client cần gửi 'name', có thể gửi 'description', 'thumbnailUrl'
    const newPlaylist = await Playlist.create(req.body);
    res.status(201).json({ success: true, data: newPlaylist });
  } catch (error) {
    console.error("Create Playlist Error:", error);
    if (error.name === 'ValidationError') {
       const messages = Object.values(error.errors).map(val => val.message);
       return res.status(400).json({ success: false, message: 'Dữ liệu không hợp lệ', errors: messages });
    }
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi tạo playlist' });
  }
};

// @desc    Update a playlist by ID (chỉ thông tin playlist, không phải bài hát)
// @route   PUT /playlists/:id
// @access  Private (Chỉ chủ sở hữu)
exports.updatePlaylist = async (req, res, next) => {
  try {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
        return res.status(400).json({ success: false, message: 'ID playlist không hợp lệ' });
    }
    let playlist = await Playlist.findById(req.params.id);
    if (!playlist) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy playlist để cập nhật' });
    }

    // TODO: Kiểm tra playlist.creator === req.user.id (chỉ chủ sở hữu được sửa)

    // Chỉ cho phép cập nhật name, description, thumbnailUrl
    const allowedUpdates = {
        name: req.body.name,
        description: req.body.description,
        thumbnailUrl: req.body.thumbnailUrl
    };
    // Loại bỏ các trường undefined để không ghi đè giá trị cũ bằng undefined
    Object.keys(allowedUpdates).forEach(key => allowedUpdates[key] === undefined && delete allowedUpdates[key]);


    playlist = await Playlist.findByIdAndUpdate(req.params.id, allowedUpdates, {
      new: true,
      runValidators: true
    });
    res.status(200).json({ success: true, data: playlist });
  } catch (error) {
    console.error("Update Playlist Error:", error);
    if (error.name === 'ValidationError') {
       const messages = Object.values(error.errors).map(val => val.message);
       return res.status(400).json({ success: false, message: 'Dữ liệu cập nhật không hợp lệ', errors: messages });
    }
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi cập nhật playlist' });
  }
};

// @desc    Delete a playlist by ID
// @route   DELETE /playlists/:id
// @access  Private (Chỉ chủ sở hữu)
exports.deletePlaylist = async (req, res, next) => {
  try {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
        return res.status(400).json({ success: false, message: 'ID playlist không hợp lệ' });
    }
    const playlist = await Playlist.findById(req.params.id);
    if (!playlist) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy playlist để xóa' });
    }

    // TODO: Kiểm tra playlist.creator === req.user.id

    await playlist.deleteOne();
    res.status(200).json({ success: true, message: 'Playlist đã được xóa', data: {} });
  } catch (error) {
    console.error("Delete Playlist Error:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi xóa playlist' });
  }
};


// --- Chức năng quản lý bài hát trong Playlist ---

// @desc    Add song to playlist
// @route   POST /playlists/:playlistId/songs
// @access  Private (Chỉ chủ sở hữu playlist)
exports.addSongToPlaylist = async (req, res, next) => {
    const { playlistId } = req.params;
    const { songId } = req.body; // Client cần gửi songId trong body

    if (!mongoose.Types.ObjectId.isValid(playlistId) || !mongoose.Types.ObjectId.isValid(songId)) {
        return res.status(400).json({ success: false, message: 'ID playlist hoặc ID bài hát không hợp lệ' });
    }

    try {
        const playlist = await Playlist.findById(playlistId);
        const song = await Song.findById(songId); // Kiểm tra bài hát tồn tại

        if (!playlist) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy playlist' });
        }
        if (!song) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy bài hát để thêm' });
        }

        // TODO: Kiểm tra playlist.creator === req.user.id

        // Kiểm tra xem bài hát đã có trong playlist chưa
        if (playlist.songs.includes(songId)) {
             return res.status(400).json({ success: false, message: 'Bài hát đã có trong playlist' });
        }

        // Thêm songId vào mảng songs và lưu lại
        playlist.songs.push(songId);
        await playlist.save();

        res.status(200).json({ success: true, message: 'Đã thêm bài hát vào playlist', data: playlist });

    } catch (error) {
        console.error("Add Song to Playlist Error:", error);
        res.status(500).json({ success: false, message: 'Lỗi máy chủ khi thêm bài hát vào playlist' });
    }
};

// @desc    Remove song from playlist
// @route   DELETE /playlists/:playlistId/songs/:songId
// @access  Private (Chỉ chủ sở hữu playlist)
exports.removeSongFromPlaylist = async (req, res, next) => {
    const { playlistId, songId } = req.params;

     if (!mongoose.Types.ObjectId.isValid(playlistId) || !mongoose.Types.ObjectId.isValid(songId)) {
        return res.status(400).json({ success: false, message: 'ID playlist hoặc ID bài hát không hợp lệ' });
    }

    try {
        const playlist = await Playlist.findById(playlistId);

        if (!playlist) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy playlist' });
        }

        // TODO: Kiểm tra playlist.creator === req.user.id

        // Kiểm tra xem bài hát có trong playlist không
        if (!playlist.songs.includes(songId)) {
             return res.status(400).json({ success: false, message: 'Bài hát không có trong playlist này' });
        }

        // Xóa songId khỏi mảng songs
        playlist.songs = playlist.songs.filter(id => id.toString() !== songId);
        await playlist.save();

         res.status(200).json({ success: true, message: 'Đã xóa bài hát khỏi playlist', data: playlist });

    } catch (error) {
         console.error("Remove Song from Playlist Error:", error);
        res.status(500).json({ success: false, message: 'Lỗi máy chủ khi xóa bài hát khỏi playlist' });
    }
};