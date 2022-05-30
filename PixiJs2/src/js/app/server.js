
!function(){
    var SERVER = {}

    function getServerURL(){
        //replace server
        return 'https://mcd-api.gululu.com/';
        //replace server
    }

    function uploadQN(file,key,token){
        return new Promise((resolve,reject)=>{
            const options = {
                quality: 0.85,
                noCompressIfLarger: true
                // maxWidth: 1000,
                // maxHeight: 618
            }
            qiniu.compressImage(file, options).then(data => {
                const observable = qiniu.upload(file, key, token)
                const subscription = observable.subscribe({
                    next(res){
                        console.log(res);
                    },
                    error(err){
                      // ...
                      reject();
                    },
                    complete(res){
                      resolve(res);
                    }
                })
            })
        })
    }
    SERVER.uploadQN = uploadQN;

    async function callApi(params){
        if(!$.request)throw new Error("pls confirm request already mounted");

        // if(!params.headers.mid){
        //     console.log("no meddyid");
        //     return null;
        // }

        const res = await $.request({
            url:getServerURL()+params.path,
            method:params.method||"POST",
            data:params.data||{},
            headers:params.headers||{}
        })

        return res;
    }
    SERVER.callApi = callApi;

    window.SERVER = SERVER;
}();