@echo off
chcp 65001
echo 导出代码到 export\all_code.txt...

if not exist export mkdir export

echo 医院药品管理系统 - 代码导出 > export\all_code.txt

echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 主应用程序 >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type src\main\java\com\example\hospital\HospitalDrugSystemApplication.java >> export\all_code.txt

echo. >> export\all_code.txt
echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 模型类 - Drug.java >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type src\main\java\com\example\hospital\model\Drug.java >> export\all_code.txt

echo. >> export\all_code.txt
echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 模型类 - User.java >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type src\main\java\com\example\hospital\model\User.java >> export\all_code.txt

echo. >> export\all_code.txt
echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 控制器 - DrugController.java >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type src\main\java\com\example\hospital\controller\DrugController.java >> export\all_code.txt

echo. >> export\all_code.txt
echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 控制器 - AuthController.java >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type src\main\java\com\example\hospital\controller\AuthController.java >> export\all_code.txt

echo. >> export\all_code.txt
echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 服务 - UserService.java >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type src\main\java\com\example\hospital\service\UserService.java >> export\all_code.txt

echo. >> export\all_code.txt
echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 存储库 - DrugRepository.java >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type src\main\java\com\example\hospital\repository\DrugRepository.java >> export\all_code.txt

echo. >> export\all_code.txt
echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 存储库 - UserRepository.java >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type src\main\java\com\example\hospital\repository\UserRepository.java >> export\all_code.txt

echo. >> export\all_code.txt
echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 配置 - SecurityConfig.java >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type src\main\java\com\example\hospital\config\SecurityConfig.java >> export\all_code.txt

echo. >> export\all_code.txt
echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 配置 - DataInitializer.java >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type src\main\java\com\example\hospital\config\DataInitializer.java >> export\all_code.txt

echo. >> export\all_code.txt
echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 前端 - App.vue >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type src\main\resources\static\js\App.vue >> export\all_code.txt

echo. >> export\all_code.txt
echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 前端 - DrugManagement.vue >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type src\main\resources\static\js\DrugManagement.vue >> export\all_code.txt

echo. >> export\all_code.txt
echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 前端 - LoginPage.vue >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type src\main\resources\static\js\LoginPage.vue >> export\all_code.txt

echo. >> export\all_code.txt
echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 前端 - main.js >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type src\main\resources\static\js\main.js >> export\all_code.txt

echo. >> export\all_code.txt
echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 前端 - vite.config.js >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type src\main\resources\static\vite.config.js >> export\all_code.txt

echo. >> export\all_code.txt
echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 前端 - index.html >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type src\main\resources\static\index.html >> export\all_code.txt

echo. >> export\all_code.txt
echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 配置 - application.properties >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type src\main\resources\application.properties >> export\all_code.txt

echo. >> export\all_code.txt
echo. >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
echo 配置 - pom.xml >> export\all_code.txt
echo =========================================================================== >> export\all_code.txt
type pom.xml >> export\all_code.txt

echo 代码已成功导出到 export\all_code.txt 