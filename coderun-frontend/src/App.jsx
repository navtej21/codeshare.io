
import { useEffect, useState, useRef } from "react";
import {Editor} from "@monaco-editor/react";

function App() {
  const [code, setCode] = useState("");

  // Store the same WebSocket instance
  const wsRef = useRef(null);

  useEffect(() => {
    // 1. Create a WebSocket connection
    const ws = new WebSocket("ws://localhost:8080/ws/room");

    // 2. Store WebSocket in ref
    wsRef.current = ws;

    // 3. When connection is successful
    ws.onopen = () => {
      console.log("Connected to WebSocket server");

      ws.send(
        JSON.stringify({
          type: "join_room",
          roomId: "room1",
        })
      );
    };

    // 4. When server sends a message
    ws.onmessage = (event) => {
      const data = JSON.parse(event.data);

      if (data.type === "edit") {
        setCode(data.content);
      }
    };

    
  }, []);

  // 6. When user changes the editor
  const handleEditorChange = (value) => {
    const newCode = value || "";

    // Update our local editor
    setCode(newCode);

    // Send change to server
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(
        JSON.stringify({
          type: "edit",
          roomId: "room1",
          content: newCode,
        })
      );
    }
  };

  return (
    <Editor
      height="90vh"
      language="python"
      value={code}
      onChange={handleEditorChange}
    />
  );
}

export default App;
