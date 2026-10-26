from flask import Flask, request, render_template, jsonify
from flask_socketio import SocketIO

app = Flask(__name__)
socketio = SocketIO(app, cors_allowed_origins="*", async_mode="threading")

current_pid = {
    "p": 0.024,
    "i": 0.0,
    "d": 0.001
}

@app.route("/")
def index():
    return render_template("index.html")
                           

@app.route("/data", methods=["POST"])
def receive_data():
    data = request.get_json(force=True)
    print("Robot data:", data)  # must print in terminal
    socketio.emit("robot_data", data)
    return "OK", 200

@app.route("/set_pid", methods=["POST"])
def set_pid():

    global current_pid

    current_pid = request.json

    print(
        "New PID:",
        current_pid
    )

    return jsonify(success=True)

@app.route("/get_pid")
def get_pid():

    return jsonify(current_pid)

print("Starting server...")

if __name__ == "__main__":
    print("Server is about to start on port 5000...")
    socketio.run(
    app,
    host="0.0.0.0",
    port=5000,
    debug=True,
    use_reloader=False
)
