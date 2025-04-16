const User = require('../models/User');

// @desc    Register a new user
// @route   POST /users/register
// @access  Public
exports.registerUser = async (req, res, next) => {
  const { username, email, password } = req.body;

  try {
    // Kiểm tra user đã tồn tại chưa
    const userExists = await User.findOne({ email });
    if (userExists) {
      return res.status(400).json({ success: false, message: 'Email đã được sử dụng' });
      // Nên có error handling middleware tốt hơn
    }
    const usernameExists = await User.findOne({ username });
     if (usernameExists) {
      return res.status(400).json({ success: false, message: 'Tên người dùng đã được sử dụng' });
    }

    // Tạo user mới (password sẽ tự hash do middleware trong model)
    const user = await User.create({ username, email, password });

    // Không trả về password
    const userResponse = await User.findById(user._id).select('-password');

    res.status(201).json({ success: true, data: userResponse });

  } catch (error) {
     console.error("Register Error:", error);
     // Trả về lỗi chung hoặc chi tiết hơn tùy vào môi trường
     res.status(500).json({ success: false, message: 'Đăng ký thất bại', error: error.message });
     // next(error); // Nếu dùng error handler middleware
  }
};

// @desc    Auth user & get token (Tạm thời chỉ trả về user data)
// @route   POST /users/login
// @access  Public
exports.loginUser = async (req, res, next) => {
  const { email, password } = req.body;

  if (!email || !password) {
     return res.status(400).json({ success: false, message: 'Vui lòng nhập email và mật khẩu' });
  }

  try {
    // Tìm user bằng email, và yêu cầu lấy cả password (vì mặc định select:false)
    const user = await User.findOne({ email }).select('+password');

    if (user && (await user.matchPassword(password))) {
      // Đăng nhập thành công
      // TODO: Tạo JWT token ở đây nếu cần

       // Trả về thông tin user (không bao gồm password)
       const userResponse = await User.findById(user._id).select('-password');
       res.status(200).json({ success: true, data: userResponse /*, token: generateToken(user._id) */ });

    } else {
       res.status(401).json({ success: false, message: 'Email hoặc mật khẩu không đúng' });
    }
  } catch (error) {
     console.error("Login Error:", error);
     res.status(500).json({ success: false, message: 'Đăng nhập thất bại', error: error.message });
     // next(error);
  }
};