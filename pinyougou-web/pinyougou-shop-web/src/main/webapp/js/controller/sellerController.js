/** 定义控制器层 */
app.controller('sellerController', function($scope, $controller, baseService){

    /** 指定继承baseController */
    $controller('baseController',{$scope:$scope});

    /** 商家申请入驻 */
    $scope.saveOrUpdate = function(){
        /** 发送post请求 */
        baseService.sendPost("/seller/save", $scope.seller)
            .then(function(response){
                if (response.data){
                    /** 跳转到登录页面 */
                    location.href = "/shoplogin.html";
                }else{
                    alert("入驻失败！");
                }
            });
    };

    /** 查询条件对象 */
    $scope.searchEntity = {};
    /** 分页查询(查询条件) */
    $scope.search = function(page, rows){
        baseService.findByPage("/seller/findByPage", page,
			rows, $scope.searchEntity)
            .then(function(response){
                /** 获取分页查询结果 */
                $scope.dataList = response.data.rows;
                /** 更新分页总记录数 */
                $scope.paginationConf.totalItems = response.data.total;
            });
    };

    /** 显示修改 */
    $scope.show = function(entity){
       /** 把json对象转化成一个新的json对象 */
       $scope.entity = JSON.parse(JSON.stringify(entity));
    };

    /** 批量删除 */
    $scope.delete = function(){
        if ($scope.ids.length > 0){
            baseService.deleteById("/seller/delete", $scope.ids)
                .then(function(response){
                    if (response.data){
                        /** 重新加载数据 */
                        $scope.reload();
                    }else{
                        alert("删除失败！");
                    }
                });
        }else{
            alert("请选择要删除的记录！");
        }
    };


    //显示商家资料
    $scope.loadSeller = function () {
        baseService.sendGet("/seller/findSeller").then(function (response) {
            $scope.entity = response.data;
        })
    };

    //修改商家资料
    $scope.update = function () {
        baseService.sendPost("/seller/update",$scope.entity).then(function (response) {
            if(response.data){
                alert("保存成功");
            }else {
                alert("修改失败");
                $scope.loadSeller();
            }
        });
    };

    //重置清空密码
    $scope.clear = function () {
        $scope.user.password = '';
        $scope.newPassword = '';
        $scope.tryPassword = '';
    };


    //修改密码
    $scope.changePasswrod = function () {
        if($scope.user.password == undefined){
            alert("请输入原密码！")
        }

        if($scope.newPassword == undefined){
            alert("请输入新密码！");
        }
        if($scope.user.password == $scope.newPassword){
            alert("新密码不能和原始密码相同，请重新输入");
            $scope.user.password = '';
            $scope.newPassword = '';
            return;
        }
        baseService.sendPost("/seller/judgePassword" , $scope.user.password).then(function (response) {
            if(response.data){
                if($scope.newPassword == $scope.tryPassword){
                    baseService.sendPost("/seller/changePassword",$scope.newPassword).then(function (resp) {
                        if(resp.data){
                            alert("密码修改成功，请重新登录");
                            location.href="/shoplogin.html";
                        }else {
                            alert("密码修改失败！");
                        }
                    });
                }else{
                    alert("新密码两次输入不一致");
                    $scope.newPassword='';
                    $scope.tryPassword='';
                }
            }else{
                alert("输入原密码有误，请重新输入！")
                $scope.clear();
            }
        });
    }

});