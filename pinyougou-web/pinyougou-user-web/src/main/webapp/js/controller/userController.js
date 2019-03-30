/** 定义控制器层 */
app.controller('userController', function ($scope, $timeout, baseService) {

    // 定义json对象
    $scope.user = {}

    // 用户注册
    $scope.save = function () {
        // 判断密码是否一致
        if ($scope.okPassword && $scope.user.password == $scope.okPassword) {
            // 发送异步请求
            baseService.sendPost("/user/save?code=" + $scope.code,
                $scope.user).then(function (response) {
                // 获取响应数据
                if (response.data) {
                    // 跳转到登录页面
                    // 清空表单数据
                    $scope.user = {};
                    $scope.okPassword = "";
                    $scope.code = "";
                } else {
                    alert("注册失败！");
                }
            });
        } else {
            alert("两次密码不一致！");
        }
    };
    /** 修改密码 */
    $scope.updatePassword = function () {
        // 判断密码是否一致
        if (!$scope.content.password || $scope.content.password != $scope.content.confirm_password) {
            alert("两次密码不一致！")
            return;
        }
        if ($scope.content.password == $scope.content.oldPassword) {
            alert("新密码不能与原密码一致！")
            return;
        }
        // 发送异步请求
        baseService.sendPost("/user/updatePassword", $scope.content).then(function (response) {
            // 获取响应数据
            if (response.data) {
                // 跳转到登录页面
                alert("密码修改成功!")
                location.href = "http://sso.pinyougou.com/login?service=http%3A%2F%2Fuser.pinyougou.com%2Flogin"
                // 清空表单数据
                $scope.content.oldPassword = "";
                $scope.content.password = "";
                $scope.content.confirm_password = "";
            } else {
                alert("密码更新失败！");
            }
        });
    }
    /** 获取手机号码 */
    $scope.getPhoneNumber = function () {
        // 发送异步请求
        baseService.sendGet("/user/findPhoneNumber").then(function (response) {
            // 获取响应数据
            $scope.user.phone = response.data.phone;
            $scope.showNumber = response.data.showNumber
        });
    }
    /** 验证身份 */
    $scope.checkPhoneNumber = function () {
        // 发送异步请求
        baseService.sendPost("/user/checkPhoneNumber?code=" + $scope.code + "&phoneCode=" + $scope.phoneCode,
            $scope.user).then(function (response) {
            // 获取响应数据
            if (response.data) {
                // 跳转到第二步 绑定新手机号
                location.href = "/home-setting-address-phone.html";
                // 清空表单数据
                $scope.phoneCode = "";
                $scope.code = "";
            } else {
                alert("验证失败！");
            }
        });
    }
    $scope.updatePhoneNumber =function () {
        // 发送异步请求
        baseService.sendPost("/user/updatePhoneNumber?code=" + $scope.code + "&phoneCode=" + $scope.phoneCode,
            $scope.user).then(function (response) {
            // 获取响应数据
            if (response.data) {
                // 跳转到第二步 绑定新手机号
                location.href = "home-setting-address-complete.html";
                // 清空表单数据
                $scope.user = "";
                $scope.phoneCode = "";
                $scope.code = "";
            } else {
                alert("绑定失败！");
            }
        });
    }
    // 定义显示文本
    $scope.tipMsg = "获取短信验证码";
    $scope.flag = false;

    // 发送短信验证码
    $scope.sendSmsCode = function () {

        // 判断手机号码的有效性
        if ($scope.user.phone && /^1[3|4|5|7|8|9]\d{9}$/.test($scope.user.phone)) {
            // 发送异步请求
            baseService.sendGet("/user/sendSmsCode?phone="
                + $scope.user.phone).then(function (response) {
                // 获取响应数据
                if (response.data) {
                    // 倒计时 (扩展)
                    $scope.flag = true;
                    // 调用倒计时方法
                    $scope.downcount(90);
                } else {
                    alert("获取短信验证码失败！");
                }
            });
        } else {
            alert("手机号码不正确！");
        }
    };


    // 倒计时方法
    $scope.downcount = function (seconds) {
        if (seconds > 0) {
            seconds--;
            $scope.tipMsg = seconds + "秒，后重新获取";

            // 开启定时器
            $timeout(function () {
                $scope.downcount(seconds);
            }, 1000);
        } else {
            $scope.tipMsg = "获取短信验证码";
            $scope.flag = false;
        }
    };

});