const express = require('express');
const dotenv = require('dotenv');
const cors = require('cors');
const connectDB = require('./config/db');

// Load env vars
dotenv.config(); // Nên đặt trước các import sử dụng process.env

// Connect to database
connectDB();

// Import routes
const userRoutes = require('./routes/userRoutes');
const songRoutes = require('./routes/songRoutes');
const albumRoutes = require('./routes/albumRoutes');
const artistRoutes = require('./routes/artistRoutes');
const playlistRoutes = require('./routes/playlistRoutes');
const videoRoutes = require('./routes/videoRoutes'); // <<< Import videoRoutes

const app = express();

// Enable CORS for all origins (for development)
// Cần cấu hình chặt chẽ hơn cho production
app.use(cors());

// Body parser Middleware (để đọc req.body dạng JSON)
app.use(express.json());

// Mount routers
app.use('/users', userRoutes);
app.use('/songs', songRoutes);
app.use('/albums', albumRoutes);
app.use('/artists', artistRoutes);
app.use('/playlists', playlistRoutes);
app.use('/videos', videoRoutes); // <<< Sử dụng videoRoutes

// Route gốc đơn giản để kiểm tra server chạy
app.get('/', (req, res) => {
  res.send('NewTube API is running...');
});

// TODO: Add Error Handling Middleware
// Ví dụ:
// const errorHandler = require('./middleware/error'); // Tạo file middleware/error.js
// app.use(errorHandler);

const PORT = process.env.PORT || 3000; // Lấy cổng từ env hoặc mặc định 3000

const server = app.listen(PORT, () =>
  console.log(
    `Server running in ${process.env.NODE_ENV || 'development'} mode on port ${PORT}` // Thêm default 'development'
  )
);

// Handle unhandled promise rejections
process.on('unhandledRejection', (err, promise) => {
  console.error(`Unhandled Rejection: ${err.message}`);
  // Close server & exit process gracefully
  server.close(() => process.exit(1));
});

// Optional: Handle SIGTERM for graceful shutdown (useful in Docker/cloud environments)
process.on('SIGTERM', () => {
    console.log('SIGTERM signal received: closing HTTP server');
    server.close(() => {
        console.log('HTTP server closed');
        // Đóng kết nối DB nếu cần
        // mongoose.connection.close(false, () => {
        //    console.log('MongoDb connection closed.');
        //    process.exit(0);
        // });
         process.exit(0);
    });
});