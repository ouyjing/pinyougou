/** 定义控制器层 */
app.controller('indexController', function ($scope, $controller,$interval, $location, baseService) {

    // brandController中的$scope 需要 继承到baseController的$scope中的方法
    $controller("baseController", {$scope: $scope});

    // 获取登录用户名
    $scope.showName = function () {
        baseService.sendGet("/user/showName").then(function (response) {
            // 获取响应数据
            $scope.loginName = response.data.loginName;
        });
    };

    /**  分页查询订单 */
    $scope.search = function () {
        // 查询条件
        //alert(JSON.stringify($scope.searchEntity));
        // 发送异步请求
        baseService.sendGet("/order/findOrderByUserId").then(function (response) {
            $scope.orderList = response.data;
        });
    };

    /** 跳转到【微信扫码支付页面】进行支付  */
    $scope.goPayPage = function (orderId) {

        $scope.orderId = orderId;
        //根据订单id,生成支付日志.
        baseService.sendGet("order/buildPayLog", "orderId=" + orderId).then(function (response) {
            if (response.data) {
                // 微信支付，跳转到支付页面
                location.href = "/pay.html?orderId=" + $scope.orderId;
            } else {
                alert("提交订单失败！");
            }
        })
    };

    //获得订单id
    $scope.orderId = $location.search().orderId;

    // 生成微信支付二维码
    $scope.genPayCode = function (orderId) {
        // 发送异步请求
        baseService.sendGet("/order/genPayCode", "orderId=" + orderId).then(function (response) {
            // 获取响应数据 {outTradeNo : '', money : 100, codeUrl : ''}
            // 获取交易订单号
            $scope.outTradeNo = response.data.outTradeNo;
            // 获取支付金额
            $scope.money = (response.data.totalFee / 100).toFixed(2);
            // 获取支付URL
            $scope.codeUrl = response.data.codeUrl;

            // 生成二维码
            document.getElementById("qrious").src = "/barcode?url=" + $scope.codeUrl;

            $scope.orderId = [];

            /**
             * 开启定时器(间隔3秒发送异步请求，获取支付状态)
             * 第一个参数：调用回调的函数
             * 第二个参数：时间毫秒数 3秒
             * 第三个参数：总调用次数 100次
             */
            var timer = $interval(function () {
                // 发送异步请求
                baseService.sendGet("/order/queryPayStatus?outTradeNo="
                    + $scope.outTradeNo).then(function (response) {
                    // 获取响应数据 {status : 1|2|3} 1: 支付成功、2:未支付、3:支付失败
                    if (response.data.status == 1) { // 支付成功
                        // 取消定时器
                        $interval.cancel(timer);
                        // 跳转到支付成功页面
                        location.href = "/paysuccess.html?money=" + $scope.money;
                    }
                    if (response.data.status == 3) {// 支付失败
                        // 取消定时器
                        $interval.cancel(timer);
                        // 跳转到支付失败页面
                        location.href = "/payfail.html";
                    }
                });
            }, 3000, 100);

            // 在总次数调用完之后，回调一个指定函数
            timer.then(function () {
                // 提示信息
                $scope.tip = "二维码已过期，刷新页面重新获取二维码。";
            });

        });
    };

    // 获取支付金额

    $scope.money = $location.search().money;


});