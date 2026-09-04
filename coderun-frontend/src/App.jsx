import { useEffect, useState, useRef } from "react";
import { Editor } from "@monaco-editor/react";

function App() {
  const [code, setCode] = useState("");
  const [testResults, setTestResults] = useState(null);
  const [executionOutput, setExecutionOutput] = useState(null);

  const wsRef = useRef(null);
  const debounceTimerRef = useRef(null);

  useEffect(() => {
    // create a websocket connection to the server
    const ws = new WebSocket("ws://localhost:8080/ws/room");

    // store the websocket instance in the ref
    wsRef.current = ws;

    ws.onopen = () => {
      console.log("Connected to WebSocket server");
      ws.send(JSON.stringify({ type: "join_room", roomId: "room1" }));
      console.log("joined Room1");
    };

    ws.onmessage = (event) => {
      const data = JSON.parse(event.data);

      if (data.type === "edit") {
        setCode(data.content);
      }

      if (data.type === "run") {
        setExecutionOutput(data.response);
        setTestResults(data.testResults);
      }
    };

    // cleanup on unmount — close socket AND cancel any pending debounced run
    return () => {
      ws.close();
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
    };
  }, []);

  // debounced auto-run function
  const debouncedRun = (newCode) => {
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    debounceTimerRef.current = setTimeout(() => {
      if (wsRef.current?.readyState === WebSocket.OPEN) {
        wsRef.current.send(
          JSON.stringify({
            type: "run",
            roomId: "room1",
            language: "python",
            content: newCode,
          })
        );
      }
    }, 1500);
  };

  const handleEditorChange = (value) => {
    const newCode = value || "";
    setCode(newCode);

    if (wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(
        JSON.stringify({
          type: "edit",
          roomId: "room1",
          content: newCode,
        })
      );
    }

    debouncedRun(newCode);
  };

  return (
    <div>
      <Editor
        height="70vh"
        language="python"
        value={code}
        onChange={handleEditorChange}
      />
      {executionOutput && (
        <div>
          <pre>Output: {executionOutput.stdOut}</pre>
          {executionOutput.stdErr && (
            <pre style={{ color: "red" }}>{executionOutput.stdErr}</pre>
          )}
        </div>
      )}
      {testResults && (
        <div>
          {testResults.map((passed, i) => (
            <span key={i} style={{ fontSize: "24px" }}>
              {passed ? "✅" : "❌"}
            </span>
          ))}
        </div>
      )}
    </div>
  );
}

export default App;