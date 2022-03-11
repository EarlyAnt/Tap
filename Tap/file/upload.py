from flask import Flask, request, jsonify

app = Flask(__name__)

@app.post("/upload")
def upload_file():
    for filename, file_obj in request.files.items():
        if file_obj and filename:
            file_obj.save(filename)
            
        file = open('file_result.txt', 'w')
        file.write(filename)
        file.close()
        
    return jsonify(msg="OK")

app.run(
      #host='192.168.0.103',
      host='0.0.0.0',
      port= 5000,
      debug=True
    )