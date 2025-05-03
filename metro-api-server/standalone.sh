#!/bin/bash

# This script runs a simple HTTP server on port 8082 that serves 
# predefined responses to mimic the Delhi Metro API

PORT=8082
echo "Starting metro-api local server on port $PORT..."

# Create a temporary directory for files
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

# Write response files
echo '["Yellow Line","Blue Line","Red Line","Green Line","Violet Line"]' > $TEMP_DIR/lines.json

cat > $TEMP_DIR/shortestPath_rajiv_kashmere.json << EOF
{
  "source": "Rajiv Chowk",
  "destination": "Kashmere Gate",
  "path": [
    "Rajiv Chowk",
    "New Delhi",
    "Chawri Bazar",
    "Chandni Chowk",
    "Kashmere Gate"
  ],
  "lines": [
    "Yellow",
    "Yellow",
    "Yellow",
    "Yellow",
    "Yellow"
  ],
  "interchanges": 0,
  "totalStations": 4
}
EOF

echo '["Rajiv Chowk", "Kashmere Gate", "Central Secretariat", "Hauz Khas", "Huda City Centre"]' > $TEMP_DIR/stations.json

# Start a simple HTTP server
cd $TEMP_DIR

# Function to handle requests
handle_request() {
  read request
  
  # Extract the path
  path=$(echo "$request" | head -1 | cut -d' ' -f2)
  
  echo "HTTP/1.1 200 OK"
  echo "Content-Type: application/json"
  echo "Access-Control-Allow-Origin: *"
  echo ""
  
  if [[ $path == "/lines" ]]; then
    cat lines.json
  elif [[ $path == "/stations" ]]; then
    cat stations.json
  elif [[ $path == "/shortestPath"* ]]; then
    if [[ $path == *"source=Rajiv%20Chowk"* && $path == *"destination=Kashmere%20Gate"* ]]; then
      cat shortestPath_rajiv_kashmere.json
    else
      echo '{"error": "Route not found"}'
    fi
  else
    echo '{"error": "Endpoint not found"}'
  fi
}

# Run the server
python3 -c "
import http.server
import socketserver

PORT = $PORT

class MyHandler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        
        if self.path == '/lines':
            with open('lines.json', 'rb') as f:
                self.wfile.write(f.read())
        elif self.path == '/stations':
            with open('stations.json', 'rb') as f:
                self.wfile.write(f.read())
        elif self.path.startswith('/shortestPath'):
            if 'source=Rajiv%20Chowk' in self.path and 'destination=Kashmere%20Gate' in self.path:
                with open('shortestPath_rajiv_kashmere.json', 'rb') as f:
                    self.wfile.write(f.read())
            else:
                self.wfile.write(b'{\"error\": \"Route not found\"}')
        else:
            self.wfile.write(b'{\"error\": \"Endpoint not found\"}')

with socketserver.TCPServer(('', PORT), MyHandler) as httpd:
    print('Server running at http://localhost:' + str(PORT))
    httpd.serve_forever()
" 