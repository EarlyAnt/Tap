from flask import Flask, request, jsonify, send_file, render_template

app = Flask(__name__)

@app.route('/home', methods = ['GET'])
def home():
    if request.method == 'GET':
        return render_template('h5_demo.html', input_text = '', res_text = '')

@app.route('/upload',methods = ['POST'])
def upload_file():
    for filename, file_obj in request.files.items():
        if file_obj and filename:
            file_obj.save(filename)
            
        #file = open('file_result.txt', 'w')
        #file.write(filename)
        #file.close()
        
    return jsonify(msg="OK")
    
@app.route('/download/<filename>')
def download_file(filename):
	return send_file(filename, as_attachment=True)

app.run(
      #host='192.168.0.103',
      host='0.0.0.0',
      port= 5000,
      debug=True
    )