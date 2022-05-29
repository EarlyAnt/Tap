!function() {
    let $ = {};
    window.$ = $;

    function init() {
        console.log("js脚本入口");
        document.getElementById("bg").style.backgroundColor = "#F90";
        console.log("3333");
    }

    console.log("1111");
    window.onload = init();
    console.log("2222");
}();
