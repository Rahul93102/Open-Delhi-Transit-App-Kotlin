#!/usr/bin/env python3

import http.server
import socketserver
import json
import os
import urllib.parse
from pathlib import Path
import shutil
import sys

# Configuration
PORT = 8082
LINES_DIR = "../app/MyApplication/app/src/lines"
ASSET_DIR = Path("../app/MyApplication/app/src")

# Initialize data structures
stations_by_line = {}
station_map = {}  # name (lowercase) -> Station object
graph = {}  # Station -> Set of connected stations
all_stations = []

# Station class
class Station:
    def __init__(self, name, line, index):
        self.name = name
        self.line = line
        self.index = index
    
    def __eq__(self, other):
        return (isinstance(other, Station) and 
                self.name == other.name and 
                self.line == other.line)
    
    def __hash__(self):
        return hash((self.name, self.line))
    
    def to_dict(self):
        return {
            "name": self.name,
            "line": self.line,
            "index": self.index
        }

# Load data from json files
def load_data():
    global stations_by_line, station_map, graph, all_stations
    
    print(f"Loading data from {LINES_DIR}")
    
    if not os.path.exists(LINES_DIR):
        print(f"Error: Directory not found: {LINES_DIR}")
        print(f"Current directory: {os.getcwd()}")
        print(f"Available files: {os.listdir('.')}")
        sys.exit(1)
    
    # Get all line files
    line_files = [f for f in os.listdir(LINES_DIR) if f.endswith('.json') and not f.startswith('station_entity')]

    for line_file in line_files:
        line_name = line_file.replace('.json', '')
        print(f"Loading line: {line_name}")
        
        try:
            with open(os.path.join(LINES_DIR, line_file), 'r', encoding='utf-8') as f:
                line_data = json.load(f)
                
            # Create stations for this line
            stations = []
            
            # All lines seem to have a consistent format in the new data
            for idx, station_obj in enumerate(line_data):
                # Extract station name from the object
                station_name = None
                
                # Try different keys that might contain station name
                for key in station_obj:
                    value = station_obj[key]
                    if isinstance(value, str) and value.strip() and not value.isdigit():
                        station_name = value.strip()
                        break
                
                if not station_name:
                    continue  # Skip if no valid name
                
                station = Station(station_name, line_name, idx)
                stations.append(station)
                all_stations.append(station)
                
                # Store in station map with lowercase name for case-insensitive lookups
                station_map[station_name.lower()] = station
            
            stations_by_line[line_name] = stations
            
        except Exception as e:
            print(f"Error loading {line_file}: {str(e)}")
    
    # Build graph connections
    build_graph()
    
    print(f"Loaded {len(all_stations)} stations across {len(stations_by_line)} lines")
    print(f"Station map contains {len(station_map)} entries")
    # Print a few sample stations for debugging
    sample_stations = list(station_map.keys())[:5]
    print(f"Sample stations: {sample_stations}")

# Build the station graph for path finding
def build_graph():
    global graph
    
    # Initialize graph
    for station in all_stations:
        graph[station] = set()
    
    # Connect stations on the same line
    for line_name, stations in stations_by_line.items():
        if len(stations) > 1:
            sorted_stations = sorted(stations, key=lambda s: s.index)
            
            # Connect adjacent stations
            for i in range(len(sorted_stations) - 1):
                add_edge(sorted_stations[i], sorted_stations[i + 1])
    
    # Connect interchange stations
    stations_by_name = {}
    for station in all_stations:
        if station.name not in stations_by_name:
            stations_by_name[station.name] = []
        stations_by_name[station.name].append(station)
    
    # Connect all stations with the same name (interchanges)
    for name, same_name_stations in stations_by_name.items():
        if len(same_name_stations) > 1:
            for i in range(len(same_name_stations)):
                for j in range(i + 1, len(same_name_stations)):
                    add_edge(same_name_stations[i], same_name_stations[j])

def add_edge(station1, station2):
    if station1 in graph:
        graph[station1].add(station2)
    else:
        graph[station1] = {station2}
    
    if station2 in graph:
        graph[station2].add(station1)
    else:
        graph[station2] = {station1}

# Find shortest path between two stations
def find_shortest_path(source_name, destination_name):
    # Get station objects
    source = get_station(source_name)
    destination = get_station(destination_name)
    
    print(f"Looking for route from {source_name} to {destination_name}")
    print(f"Source station found: {source}")
    print(f"Destination station found: {destination}")
    
    if not source or not destination:
        print(f"Could not find one of the stations: source={bool(source)}, destination={bool(destination)}")
        return None
    
    # If source and destination are the same
    if source.name.lower() == destination.name.lower():
        return {
            "source": source.name,
            "destination": destination.name,
            "path": [source.name],
            "lines": [source.line],
            "interchanges": 0,
            "totalStations": 0
        }
    
    # Dijkstra's algorithm
    from queue import PriorityQueue
    
    visited = set()
    distances = {station: float('infinity') for station in all_stations}
    distances[source] = 0
    previous = {}
    
    pq = PriorityQueue()
    pq.put((0, source))
    
    while not pq.empty():
        current_distance, current = pq.get()
        
        if current == destination:
            break
        
        if current in visited:
            continue
        
        visited.add(current)
        
        if current not in graph:
            print(f"Warning: Station {current.name} not in graph!")
            continue
        
        # Process neighbors
        for neighbor in graph[current]:
            if neighbor in visited:
                continue
            
            # 1 unit for same line, 3 units for interchange
            edge_weight = 1 if current.line == neighbor.line else 3
            distance = current_distance + edge_weight
            
            if distance < distances[neighbor]:
                distances[neighbor] = distance
                previous[neighbor] = current
                pq.put((distance, neighbor))
    
    # Reconstruct path
    if destination not in previous and source != destination:
        print(f"No path found in graph from {source.name} to {destination.name}")
        # Print some debug info
        print(f"Graph connections for source: {[s.name for s in graph.get(source, [])]}")
        print(f"Graph size: {len(graph)} stations connected")
        return None
    
    path = []
    current = destination
    
    while current != source:
        path.append(current)
        current = previous[current]
    
    path.append(source)
    path.reverse()
    
    # Count interchanges
    interchanges = 0
    for i in range(1, len(path)):
        if path[i].line != path[i-1].line:
            interchanges += 1
    
    return {
        "source": source.name,
        "destination": destination.name,
        "path": [station.name for station in path],
        "lines": [station.line for station in path],
        "interchanges": interchanges,
        "totalStations": len(path) - 1
    }

def get_station(name):
    normalized_name = name.lower().strip()
    return station_map.get(normalized_name)

def search_stations(query):
    normalized_query = query.lower().strip()
    if not normalized_query:
        return []
    
    # Exact matches
    exact_matches = [station for station in all_stations if station.name.lower() == normalized_query]
    if exact_matches:
        return exact_matches
    
    # Contains matches
    contains_matches = [station for station in all_stations if normalized_query in station.name.lower()]
    if contains_matches:
        return contains_matches
    
    # Word matches
    if ' ' in normalized_query:
        query_words = [word for word in normalized_query.split() if len(word) > 1]
        return [station for station in all_stations if any(word in station.name.lower() for word in query_words)]
    
    return []

# HTTP request handler
class MetroAPIHandler(http.server.BaseHTTPRequestHandler):
    def _set_headers(self, content_type="application/json"):
        self.send_response(200)
        self.send_header('Content-Type', content_type)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
    
    def _send_json_response(self, data):
        self._set_headers()
        self.wfile.write(json.dumps(data).encode())
    
    def do_GET(self):
        parsed_path = urllib.parse.urlparse(self.path)
        params = urllib.parse.parse_qs(parsed_path.query)
        
        print(f"Received request: {parsed_path.path} with params: {params}")
        
        # Routes
        if parsed_path.path == '/lines':
            self._send_json_response(list(stations_by_line.keys()))
            
        elif parsed_path.path == '/stations':
            self._send_json_response([station.name for station in all_stations])
            
        elif parsed_path.path == '/stationsByLine':
            if 'line' in params:
                line = params['line'][0]
                stations = stations_by_line.get(line, [])
                self._send_json_response([station.name for station in stations])
            else:
                self._send_json_response({"error": "Missing line parameter"})
        
        elif parsed_path.path == '/shortestPath':
            if 'source' in params and 'destination' in params:
                source = params['source'][0]
                destination = params['destination'][0]
                print(f"Finding shortest path from {source} to {destination}")
                path = find_shortest_path(source, destination)
                
                if path:
                    self._send_json_response(path)
                else:
                    print(f"No path found between {source} and {destination}")
                    self._send_json_response({"error": "Route not found"})
            else:
                self._send_json_response({"error": "Missing source or destination parameters"})
                
        elif parsed_path.path == '/searchStations':
            if 'query' in params:
                query = params['query'][0]
                stations = search_stations(query)
                self._send_json_response([station.to_dict() for station in stations])
            else:
                self._send_json_response([])
                
        else:
            self._send_json_response({"error": "Endpoint not found"})
    
    def log_message(self, format, *args):
        # Custom logging to show requests
        print(f"{self.client_address[0]} - {args[0]}")

def main():
    try:
        # Load data
        load_data()
        
        # Start HTTP server
        handler = MetroAPIHandler
        httpd = socketserver.TCPServer(("", PORT), handler)
        
        print(f"Server running at http://localhost:{PORT}")
        httpd.serve_forever()
        
    except KeyboardInterrupt:
        print("Shutting down server")
        httpd.server_close()

if __name__ == "__main__":
    main() 