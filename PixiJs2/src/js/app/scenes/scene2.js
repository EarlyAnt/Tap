this.scene2 = function (gender) {
    overlay.visible = true;

    let scene = new PIXI.Container();
    mainContainer.addChild(scene);

    let camera_tips = new PIXI.spine.Spine(getAsset("assets/scene2/camera_tips.json").spineData);
    camera_tips.autoUpdate = true;
    camera_tips.state.timeScale = 1.6;
    scene.addChild(camera_tips);
    camera_tips.y = -742 + (sysInfo.viewport.height - 942) / 2 - 300

    camera_tips.state.setAnimation(0, 'animation', true);

    let btnUpload = document.getElementById("btnUpload");
    btnUpload.style.display = '';

    let tips = new PIXI.Sprite(getAsset("assets/scene2/tips.png").texture);
    scene.addChild(tips);
    tips.x = 88;
    tips.y = camera_tips.y + 942 + 35 + 742;
}