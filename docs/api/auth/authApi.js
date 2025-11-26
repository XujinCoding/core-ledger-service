import request from '@/utils/request'

/**
 * Wechat Mini Program Login
 * @param {Object} data - Login data
 * @param {string} data.code - Wechat login code (required)
 * @param {string} data.encryptedData - Encrypted data containing phone number
 * @param {string} data.iv - Initialization vector for encryption
 * @param {string} data.nickname - Wechat nickname
 * @param {string} data.avatarUrl - Wechat avatar URL
 * @returns {Promise<{
 *   code: number,
 *   message: string,
 *   data: {
 *     token: string,
 *     userInfo: {
 *       id: number,
 *       username: string,
 *       phone: string,
 *       role: number,
 *       roleDesc: string,
 *       wxNickname: string,
 *       wxAvatarUrl: string
 *     },
 *     needSupplement: boolean,
 *     isNewUser: boolean,
 *     tempOpenid: string,
 *     expireTime: string
 *   },
 *   timestamp: number
 * }>}
 */
export function wechatLogin(data) {
  return request({
    url: '/api/auth/wechat-login',
    method: 'post',
    data
  })
}

/**
 * Supplement User Information (for existing users)
 * @param {Object} data - User information
 * @param {string} data.openid - Wechat OpenID (required)
 * @param {string} data.phone - Phone number (required)
 * @param {string} data.nickname - Wechat nickname
 * @param {string} data.avatarUrl - Wechat avatar URL
 * @param {string} data.username - Username
 * @returns {Promise<{
 *   code: number,
 *   message: string,
 *   data: {
 *     token: string,
 *     userInfo: {
 *       id: number,
 *       username: string,
 *       phone: string,
 *       role: number,
 *       roleDesc: string,
 *       wxNickname: string,
 *       wxAvatarUrl: string
 *     },
 *     needSupplement: boolean,
 *     isNewUser: boolean,
 *     tempOpenid: string,
 *     expireTime: string
 *   },
 *   timestamp: number
 * }>}
 */
export function supplementUserInfo(data) {
  return request({
    url: '/api/auth/supplement-info',
    method: 'post',
    data
  })
}

/**
 * Register New User
 * @param {Object} data - User registration information
 * @param {string} data.openid - Wechat OpenID (required)
 * @param {string} data.phone - Phone number (required)
 * @param {string} data.nickname - Wechat nickname
 * @param {string} data.avatarUrl - Wechat avatar URL
 * @param {string} data.username - Username
 * @returns {Promise<{
 *   code: number,
 *   message: string,
 *   data: {
 *     token: string,
 *     userInfo: {
 *       id: number,
 *       username: string,
 *       phone: string,
 *       role: number,
 *       roleDesc: string,
 *       wxNickname: string,
 *       wxAvatarUrl: string
 *     },
 *     needSupplement: boolean,
 *     isNewUser: boolean,
 *     tempOpenid: string,
 *     expireTime: string
 *   },
 *   timestamp: number
 * }>}
 */
export function registerUser(data) {
  return request({
    url: '/api/auth/register',
    method: 'post',
    data
  })
}

/**
 * Phone and Password Login (for admin portal)
 * @param {Object} data - Login credentials
 * @param {string} data.phone - Phone number (required)
 * @param {string} data.password - Password (required)
 * @returns {Promise<{
 *   code: number,
 *   message: string,
 *   data: {
 *     token: string,
 *     userInfo: {
 *       id: number,
 *       username: string,
 *       phone: string,
 *       role: number,
 *       roleDesc: string,
 *       wxNickname: string,
 *       wxAvatarUrl: string
 *     },
 *     needSupplement: boolean,
 *     isNewUser: boolean,
 *     tempOpenid: string,
 *     expireTime: string
 *   },
 *   timestamp: number
 * }>}
 */
export function login(data) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

/**
 * Logout
 * @param {string} token - Authorization token (optional, can be passed via header)
 * @returns {Promise<{
 *   code: number,
 *   message: string,
 *   data: null,
 *   timestamp: number
 * }>}
 */
export function logout(token) {
  return request({
    url: '/api/auth/logout',
    method: 'post',
    headers: token ? { 'Authorization': token } : {}
  })
}

/**
 * Get Current User Information
 * @param {string} token - Authorization token (required)
 * @returns {Promise<{
 *   code: number,
 *   message: string,
 *   data: {
 *     id: number,
 *     username: string,
 *     phone: string,
 *     role: number,
 *     roleDesc: string,
 *     wxNickname: string,
 *     wxAvatarUrl: string
 *   },
 *   timestamp: number
 * }>}
 */
export function getCurrentUser(token) {
  return request({
    url: '/api/auth/current-user',
    method: 'get',
    headers: { 'Authorization': token }
  })
}
