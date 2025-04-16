const Song = require('../models/Song'); // Đảm bảo đường dẫn đúng
const mongoose = require('mongoose'); // Cần để kiểm tra ObjectId hợp lệ
const fs = require('fs'); // Module xử lý file hệ thống (cho streaming)
const path = require('path'); // Module xử lý đường dẫn (cho streaming)

// Helper để lấy MIME type dựa trên phần mở rộng file
const getMimeType = (filePath) => {
    const ext = path.extname(filePath).toLowerCase();
    switch (ext) {
        case '.mp3': return 'audio/mpeg';
        case '.m4a': return 'audio/mp4'; // Hoặc 'audio/aac'
        case '.ogg': return 'audio/ogg';
        case '.wav': return 'audio/wav';
        // Thêm các định dạng khác nếu cần
        default: return 'application/octet-stream'; // Mặc định nếu không biết
    }
};


// @desc    Get all songs
// @route   GET /songs
// @access  Public
exports.getAllSongs = async (req, res, next) => {
  try {
    const songs = await Song.find({}); // Lấy tất cả bài hát
    // TODO: Thêm phân trang (pagination) cho API thực tế
    res.status(200).json({
      success: true,
      count: songs.length,
      data: songs
    });
  } catch (error) {
    console.error("Get All Songs Error:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi lấy danh sách bài hát' });
    // next(error); // Nếu dùng error handler middleware
  }
};

// @desc    Get single song by ID
// @route   GET /songs/:id
// @access  Public
exports.getSongById = async (req, res, next) => {
  try {
    // Kiểm tra ID có hợp lệ không
    if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
        return res.status(400).json({ success: false, message: 'ID bài hát không hợp lệ' });
    }

    const song = await Song.findById(req.params.id);

    if (!song) {
      // Không tìm thấy bài hát với ID này
      return res.status(404).json({ success: false, message: 'Không tìm thấy bài hát' });
    }

    res.status(200).json({
      success: true,
      data: song
    });
  } catch (error) {
    console.error("Get Song By ID Error:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi lấy thông tin bài hát' });
    // next(error);
  }
};

// @desc    Create a new song
// @route   POST /songs
// @access  Private (Nên thêm middleware xác thực sau này)
exports.createSong = async (req, res, next) => {
  try {
    // Lấy dữ liệu từ body của request
    // Client cần gửi đủ các trường required: title, duration, filePath
    const songData = req.body;

    // Tạo bài hát mới trong DB
    const newSong = await Song.create(songData);

    res.status(201).json({ // 201 Created
      success: true,
      data: newSong
    });
  } catch (error) {
    console.error("Create Song Error:", error);
    if (error.name === 'ValidationError') {
       // Lỗi do dữ liệu gửi lên không hợp lệ (thiếu trường, sai kiểu...)
       const messages = Object.values(error.errors).map(val => val.message);
       return res.status(400).json({ success: false, message: 'Dữ liệu không hợp lệ', errors: messages });
    } else {
       res.status(500).json({ success: false, message: 'Lỗi máy chủ khi tạo bài hát' });
    }
    // next(error);
  }
};

// @desc    Update a song by ID
// @route   PUT /songs/:id
// @access  Private (Nên thêm middleware xác thực sau này)
exports.updateSong = async (req, res, next) => {
  try {
     // Kiểm tra ID có hợp lệ không
     if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
        return res.status(400).json({ success: false, message: 'ID bài hát không hợp lệ' });
     }

    let song = await Song.findById(req.params.id);

    if (!song) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy bài hát để cập nhật' });
    }

    // TODO: Kiểm tra quyền sở hữu bài hát nếu cần thiết (user có quyền sửa bài hát này không?)

    // Cập nhật bài hát với dữ liệu mới từ req.body
    song = await Song.findByIdAndUpdate(req.params.id, req.body, {
      new: true,
      runValidators: true
    });

    res.status(200).json({
      success: true,
      data: song
    });
  } catch (error) {
    console.error("Update Song Error:", error);
     if (error.name === 'ValidationError') {
       const messages = Object.values(error.errors).map(val => val.message);
       return res.status(400).json({ success: false, message: 'Dữ liệu cập nhật không hợp lệ', errors: messages });
    } else {
       res.status(500).json({ success: false, message: 'Lỗi máy chủ khi cập nhật bài hát' });
    }
    // next(error);
  }
};

// @desc    Delete a song by ID
// @route   DELETE /songs/:id
// @access  Private (Nên thêm middleware xác thực sau này)
exports.deleteSong = async (req, res, next) => {
  try {
     // Kiểm tra ID có hợp lệ không
     if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
        return res.status(400).json({ success: false, message: 'ID bài hát không hợp lệ' });
     }

    const song = await Song.findById(req.params.id);

    if (!song) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy bài hát để xóa' });
    }

    // TODO: Kiểm tra quyền sở hữu bài hát nếu cần thiết

    // Xóa bài hát
    await song.deleteOne(); // hoặc Song.findByIdAndDelete(req.params.id);

    res.status(200).json({
      success: true,
      message: 'Bài hát đã được xóa thành công',
      data: {} // Trả về data rỗng hoặc id của bài hát vừa xóa
    });
  } catch (error) {
    console.error("Delete Song Error:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi xóa bài hát' });
    // next(error);
  }
};


// @desc    Stream a song
// @route   GET /songs/:id/stream
// @access  Public (Hoặc Private nếu cần kiểm tra quyền nghe)
exports.streamSong = async (req, res, next) => {
    try {
        if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
            return res.status(400).json({ success: false, message: 'ID bài hát không hợp lệ' });
        }

        const song = await Song.findById(req.params.id);

        if (!song || !song.filePath) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy bài hát hoặc đường dẫn file' });
        }

        const filePath = song.filePath;

        // --- Trường hợp 1: filePath là URL ---
        if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
            console.log(`Redirecting to external URL: ${filePath}`);
            return res.redirect(302, filePath);
        }

        // --- Trường hợp 2: filePath là đường dẫn file cục bộ ---
        // !! QUAN TRỌNG: Điều chỉnh logic này cho phù hợp với cấu trúc thư mục và cách lưu filePath !!
        // Ví dụ: Nếu file nằm trong 'project_root/public/music/' và filePath chỉ là 'song.mp3'
        // const absoluteFilePath = path.join(__dirname, '..', 'public', 'music', filePath);
        const absoluteFilePath = path.resolve(filePath); // Tạm thời dùng resolve, cần kiểm tra kỹ!

        fs.stat(absoluteFilePath, (err, stats) => {
            if (err) {
                console.error(`File Stat Error for ${absoluteFilePath}:`, err);
                if (err.code === 'ENOENT') {
                    return res.status(404).json({ success: false, message: 'Không tìm thấy file nhạc trên server' });
                }
                return res.status(500).json({ success: false, message: 'Lỗi khi truy cập file nhạc' });
            }

            const fileSize = stats.size;
            const range = req.headers.range;
            const mimeType = getMimeType(absoluteFilePath);

            if (range) {
                const parts = range.replace(/bytes=/, "").split("-");
                const start = parseInt(parts[0], 10);
                const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;

                if (start >= fileSize || end >= fileSize || start > end) {
                    console.warn(`Invalid Range requested: ${range} for file size ${fileSize}`);
                    res.status(416).send('Requested Range Not Satisfiable');
                    return;
                }

                const chunksize = (end - start) + 1;
                const fileStream = fs.createReadStream(absoluteFilePath, { start, end });
                const head = {
                    'Content-Range': `bytes ${start}-${end}/${fileSize}`,
                    'Accept-Ranges': 'bytes',
                    'Content-Length': chunksize,
                    'Content-Type': mimeType,
                };
                console.log(`Streaming Range: bytes ${start}-${end}/${fileSize}`);
                res.writeHead(206, head); // 206 Partial Content
                fileStream.pipe(res);
                fileStream.on('error', (streamErr) => {
                    console.error('File Stream Error (Range):', streamErr);
                    res.end();
                });

            } else {
                const head = {
                    'Content-Length': fileSize,
                    'Content-Type': mimeType,
                    'Accept-Ranges': 'bytes',
                };
                console.log(`Streaming Full File: ${fileSize} bytes`);
                res.writeHead(200, head); // 200 OK
                const fileStream = fs.createReadStream(absoluteFilePath);
                fileStream.pipe(res);
                 fileStream.on('error', (streamErr) => {
                    console.error('File Stream Error (Full):', streamErr);
                    // Tránh gửi lỗi JSON nếu header đã gửi
                    if (!res.headersSent) {
                       res.status(500).json({ success: false, message: 'Lỗi khi đọc file nhạc' });
                    } else {
                       res.end();
                    }
                });
            }
        });

    } catch (error) {
        console.error("Stream Song Error:", error);
        if (!res.headersSent) {
            res.status(500).json({ success: false, message: 'Lỗi máy chủ khi xử lý yêu cầu stream' });
        } else {
            console.error("Error occurred after headers sent during streaming.");
            res.end();
        }
        // next(error);
    }
};