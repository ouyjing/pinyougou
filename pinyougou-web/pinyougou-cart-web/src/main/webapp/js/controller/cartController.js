// 购物车控制器
app.controller('cartController', function ($scope, $controller,$location, baseService) {
    // 继承baseController
    $controller('baseController', {$scope:$scope});

    /** 批量结算 */
    $scope.payment = function(){
        if ($scope.goodsItem.length > 0){
            // 把entity的json对象转化成一个新的json对象
            /** 跳转结算页面 */
            location.href = "/order/getOrderInfo.html";
        }else{
            alert("请选择要结算的商品！");
        }
    };

    /** 定义选中的商品数组 */
    $scope.goodsItem = [];

    // 定义选中的结算商品数据
    $scope.payGoods = {totalNum : 0, totalMoney : 0};

    /** 为复选框绑定点击事件 */
    $scope.updateSelection = function($event,orderItem) {
        // 把entity的json对象转化成一个新的json对象
        $scope.orderItem = JSON.parse(JSON.stringify(orderItem));
        /** 如果是被选中,则增加到数组 */
        if($event.target.checked){

            $scope.goodsItem.push(orderItem);
            // 统计购买总金额
            $scope.totalEntity.totalMoney += $scope.orderItem.totalFee;
            // 统计购买总数量
            $scope.totalEntity.totalNum += $scope.orderItem.num;
        }else{
            // 统计购买总金额
            $scope.totalEntity.totalMoney -= $scope.orderItem.totalFee;
            // 统计购买总数量
            $scope.totalEntity.totalNum -= $scope.orderItem.num;
            var idx = $scope.goodsItem.indexOf(orderItem);
            /** 删除数组中的元素  */
            $scope.goodsItem.splice(idx, 1);
        }
    };

    // 定义json对象封装统计的结果
    $scope.totalEntity = {totalNum : 0, totalMoney : 0};

    // 查询用户的购物车
    $scope.findCart = function () {
        baseService.sendGet("/cart/findCart").then(function(response){
            // 获取响应数据
            $scope.carts = response.data;


            // 循环用户的购物车数组
            for (var i = 0; i < $scope.carts.length; i++){
                // 获取数组中的元素(一个商家的购物车)
                var cart = $scope.carts[i];
                // 循环该商家的购物车数组
                for (var j = 0; j < cart.orderItems.length; j++){
                    // 获取一个商品
                    var orderItem = cart.orderItems[j];

                    // 统计购买总数量
                    $scope.totalCartNum += orderItem.num;

                }
            }
        });
    };

    // 购买数量增减与删除
    $scope.addCart = function (itemId, num) {
        baseService.sendGet("/cart/addCart?itemId="
            + itemId + "&num=" + num).then(function(response){
            // 获取响应数据
            if (response.data){
                // 重新加载购物车数据
                $scope.findCart();
            }
        });
    };
    
});