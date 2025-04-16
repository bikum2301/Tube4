const Video = require('../models/Video'); // Import Video model
const mongoose = require('mongoose');
const fs = require('fs');
const path = require('path');

// Helper lấy MIME type cho video
const getVideoMimeType = (filePath) => {
    const ext = path.extname(filePath).toLowerCase();
    switch (ext) {
        case '.mp4': return 'video/mp4';
        case '.webm': return 'video/webm';
        case '.ogg': return 'video/ogg'; // .ogv thường dùng cho video
        case '.mov': return 'video/quicktime';
        case '.avi': return 'video/x-msvideo'; // Hỗ trợ kém trên web
        case '.mkv': return 'video/x-matroska'; // Hỗ trợ kém trên web
        // Thêm các định dạng khác nếu cần
        default: return 'application/octet-stream';
    }
};

// --- CRUD Functions (Tương tự như Song/Album) ---

// @desc    Get all videos
// @route   GET /videos
// @access  Public
exports.getAllVideos = async (req, res, next) => {
  try {
    const videos = await Video.find({}); // Lấy tất cả video
    // TODO: Add pagination, filtering, sorting
    res.status(200).json({
      success: true,
      count: videos.length,
      data: videos
    });
  } catch (error) {
    console.error("Get All Videos Error:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi lấy danh sách video' });
  }
};

// @desc    Get single video by ID
// @route   GET /videos/:id
// @access  Public
exports.getVideoById = async (req, res, next) => {
  try {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
        return res.status(400).json({ success: false, message: 'ID video không hợp lệ' });
    }
    const video = await Video.findById(req.params.id); //.populate('uploader', 'username');

    if (!video) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy video' });
    }
    // TODO: Tăng view count nếu cần
    res.status(200).json({ success: true, data: video });
  } catch (error) {
    console.error("Get Video By ID Error:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi lấy thông tin video' });
  }
};

// @desc    Create a new video
// @route   POST /videos
// @access  Private (Nên bảo vệ sau)
exports.createVideo = async (req, res, next) => {
  try {
    // TODO: Gán uploader = req.user.id nếu có auth
    const newVideo = await Video.create(req.body); // Dữ liệu từ client
    res.status(201).json({ success: true, data: newVideo });
  } catch (error) {
    console.error("Create Video Error:", error);
    if (error.name === 'ValidationError') {
       const messages = Object.values(error.errors).map(val => val.message);
       return res.status(400).json({ success: false, message: 'Dữ liệu không hợp lệ', errors: messages });
    }
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi tạo video' });
  }
};

// @desc    Update a video by ID
// @route   PUT /videos/:id
// @access  Private (Nên bảo vệ sau)
exports.updateVideo = async (req, res, next) => {
  try {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
        return res.status(400).json({ success: false, message: 'ID video không hợp lệ' });
    }
    let video = await Video.findById(req.params.id);
    if (!video) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy video để cập nhật' });
    }
    // TODO: Check ownership (video.uploader === req.user.id)

    video = await Video.findByIdAndUpdate(req.params.id, req.body, {
      new: true,
      runValidators: true
    });
    res.status(200).json({ success: true, data: video });
  } catch (error) {
    console.error("Update Video Error:", error);
     if (error.name === 'ValidationError') {
       const messages = Object.values(error.errors).map(val => val.message);
       return res.status(400).json({ success: false, message: 'Dữ liệu cập nhật không hợp lệ', errors: messages });
    }
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi cập nhật video' });
  }
};

// @desc    Delete a video by ID
// @route   DELETE /videos/:id
// @access  Private (Nên bảo vệ sau)
exports.deleteVideo = async (req, res, next) => {
  try {
    if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
        return res.status(400).json({ success: false, message: 'ID video không hợp lệ' });
    }
    const video = await Video.findById(req.params.id);
    if (!video) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy video để xóa' });
    }
    // TODO: Check ownership

    // TODO: Xóa file video và thumbnail khỏi server/cloud storage nếu cần
    await video.deleteOne();
    res.status(200).json({ success: true, message: 'Video đã được xóa', data: {} });
  } catch (error) {
    console.error("Delete Video Error:", error);
    res.status(500).json({ success: false, message: 'Lỗi máy chủ khi xóa video' });
  }
};


// --- Streaming Function ---

// @desc    Stream a video
// @route   GET /videos/:id/stream
// @access  Public (Hoặc Private nếu cần kiểm tra quyền xem)
exports.streamVideo = async (req, res, next) => {
    try {
        if (!mongoose.Types.ObjectId.isValid(req.params.id)) {
            return res.status(400).json({ success: false, message: 'ID video không hợp lệ' });
        }

        const video = await Video.findById(req.params.id);

        if (!video || !video.filePath) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy video hoặc đường dẫn file' });
        }

        const filePath = video.filePath;

        // --- Trường hợp 1: filePath là URL ---
        if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
            console.log(`Redirecting video to external URL: ${filePath}`);
            return res.redirect(302, filePath);
        }

        // --- Trường hợp 2: filePath là đường dẫn file cục bộ ---
        // !! QUAN TRỌNG: Điều chỉnh logic này cho phù hợp với cấu trúc thư mục và cách lưu filePath !!
        const absoluteFilePath = path.resolve(filePath); // Cần kiểm tra kỹ!

        fs.stat(absoluteFilePath, (err, stats) => {
            if (err) {
                console.error(`Video File Stat Error for ${absoluteFilePath}:`, err);
                if (err.code === 'ENOENT') {
                    return res.status(404).json({ success: false, message: 'Không tìm thấy file video trên server' });
                }
                return res.status(500).json({ success: false, message: 'Lỗi khi truy cập file video' });
            }

            const fileSize = stats.size;
            const range = req.headers.range;
            const mimeType = getVideoMimeType(absoluteFilePath); // Dùng helper cho video

            // --- Xử lý Range Requests (Quan trọng cho video seeking) ---
            if (range) {
                const parts = range.replace(/bytes=/, "").split("-");
                const start = parseInt(parts[0], 10);
                const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1; // Stream đến cuối nếu không có end

                 // Kiểm tra tính hợp lệ của range
                if (start >= fileSize || end >= fileSize || start > end) {
                    console.warn(`Invalid Video Range requested: ${range} for file size ${fileSize}`);
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
                console.log(`Streaming Video Range: bytes ${start}-${end}/${fileSize}`);
                res.writeHead(206, head); // 206 Partial Content
                fileStream.pipe(res);
                fileStream.on('error', (streamErr) => {
                    console.error('Video File Stream Error (Range):', streamErr);
                    res.end(); // Khó gửi lỗi khác khi header đã đi
                });

            } else {
                 // --- Trường hợp không có Range (Stream toàn bộ file) ---
                const head = {
                    'Content-Length': fileSize,
                    'Content-Type': mimeType,
                    'Accept-Ranges': 'bytes', // Vẫn báo client biết là hỗ trợ range
                };
                console.log(`Streaming Full Video File: ${fileSize} bytes`);
                res.writeHead(200, head); // 200 OK
                const fileStream = fs.createReadStream(absoluteFilePath);
                fileStream.pipe(res);
                 fileStream.on('error', (streamErr) => {
                    console.error('Video File Stream Error (Full):', streamErr);
                    if (!res.headersSent) {
                       res.status(500).json({ success: false, message: 'Lỗi khi đọc file video' });
                    } else {
                       res.end();
                    }
                });
            }
        });

    } catch (error) {
        console.error("Stream Video Error:", error);
        if (!res.headersSent) {
            res.status(500).json({ success: false, message: 'Lỗi máy chủ khi xử lý yêu cầu stream video' });
        } else {
             console.error("Error occurred after headers sent during video streaming.");
            res.end();
        }
    }
};