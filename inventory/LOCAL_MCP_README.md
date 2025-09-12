# Local METRC Documentation MCP Server

This implementation provides a local MCP (Model Context Protocol) server that allows your chatbot to search and access METRC documentation from files uploaded to the `uploads/metrcdocs` folder.

## Features

- **Local Document Storage**: Store METRC documentation files in the `uploads/metrcdocs` folder
- **Full-Text Search**: Search through uploaded documents using the MCP protocol
- **Document Management**: Upload, delete, and manage documents through a web interface
- **Automatic Indexing**: Documents are automatically indexed for fast search
- **Fallback Support**: Falls back to external METRC MCP server if local documents don't contain relevant information
- **Claude Integration**: Uses Claude AI to provide intelligent answers based on local documentation
- **Multi-Format Text Extraction**:
  - **Text files** (`.txt`) - Direct text extraction
  - **Markdown files** (`.md`) - Direct text extraction with formatting preserved
  - **PDF files** (`.pdf`) - Text extraction using Apache PDFBox
  - **Word documents** (`.doc`, `.docx`) - Text extraction using Apache POI

## File Structure

```
inventory/
├── uploads/
│   ├── metrcdocs/                    # Document storage folder
│   │   ├── sample-metrc-documentation.md
│   │   └── [your uploaded files]
│   └── mcp/
│       ├── metrc-mcp-connector.dxt   # External MCP connector
│       └── local-metrcdocs-connector.dxt  # Local MCP connector
├── src/main/java/com/techvvs/inventory/
│   ├── metrcdocs/
│   │   ├── DocumentIndexService.java      # Document indexing service
│   │   ├── LocalMcpServerController.java  # Local MCP server
│   │   ├── LocalMetrcMcpClient.java       # Local MCP client
│   │   └── LocalDocsService.java          # Local docs service
│   └── controller/
│       └── MetrcdocsManagementController.java  # Document management
└── src/main/resources/templates/
    └── metrcdocs/
        └── manage.html              # Document management UI
```

## How It Works

### 1. Document Storage
- Documents are stored in `uploads/metrcdocs/`
- Supported formats: `.txt`, `.md`, `.pdf`, `.doc`, `.docx`
- Text is automatically extracted from all document types using appropriate libraries:
  - **PDFBox** for PDF text extraction
  - **Apache POI** for Word document text extraction
- Documents are automatically indexed when uploaded

### 2. MCP Server
- Local MCP server runs at `http://localhost:8080/api/mcp`
- Implements the MCP protocol for tool calls and resource access
- Provides search functionality through the `search_metrcdocs` tool

### 3. Chatbot Integration
- Chatbot prioritizes local documents over external sources
- Falls back to external METRC MCP server if local documents don't contain relevant information
- Uses Claude AI to provide intelligent answers based on found documentation

## Usage

### 1. Upload Documents

#### Via Web Interface
1. Navigate to `/metrcdocs/manage`
2. Drag and drop files or click to select files
3. Documents are automatically indexed

#### Via File System
1. Place documents in `uploads/metrcdocs/`
2. Call the reindex endpoint: `POST /metrcdocs/reindex`

### 2. Use the Chatbot
1. Navigate to `/chatbot`
2. Ask questions about METRC documentation
3. The system will search local documents first, then fall back to external sources

### 3. Manage Documents
- **View Documents**: `/metrcdocs/manage`
- **Upload Documents**: `POST /metrcdocs/upload`
- **Delete Documents**: `DELETE /metrcdocs/document/{documentId}`
- **Reindex Documents**: `POST /metrcdocs/reindex`
- **Search Documents**: `GET /metrcdocs/search?query={searchTerm}`
- **Get Status**: `GET /metrcdocs/status`

## API Endpoints

### MCP Server Endpoints
- `POST /api/mcp` - Main MCP endpoint
- `GET /api/mcp/status` - MCP server status
- `POST /api/mcp/reindex` - Reindex documents

### Management Endpoints
- `GET /metrcdocs/manage` - Document management page
- `POST /metrcdocs/upload` - Upload documents
- `DELETE /metrcdocs/document/{documentId}` - Delete document
- `POST /metrcdocs/reindex` - Reindex documents
- `GET /metrcdocs/search` - Search documents
- `GET /metrcdocs/status` - Get system status

## Configuration

### Application Properties
```properties
# Local METRC Documentation Configuration
metrcdocs.folder.path=uploads/metrcdocs
metrc.mcp.local.url=http://localhost:8080/api/mcp
```

### MCP Connector File
The local MCP connector is defined in `uploads/mcp/local-metrcdocs-connector.dxt`:

```json
{
  "name": "Local METRC Documentation Server",
  "description": "Local MCP server for searching and accessing METRC documentation from uploaded files",
  "mcp": {
    "transport": {
      "type": "http",
      "url": "http://localhost:8080/api/mcp"
    },
    "capabilities": {
      "tools": true,
      "resources": true
    }
  }
}
```

## MCP Tools

### search_metrcdocs
Searches through local METRC documentation files.

**Parameters:**
- `query` (string, required): Search query

**Response:**
Returns matching document content in MCP format.

### get_metrcdoc
Retrieves a specific document by ID.

**Parameters:**
- `documentId` (string, required): Document ID

**Response:**
Returns the document content in MCP format.

## Example Usage

### 1. Upload a Document
```bash
curl -X POST -F "file=@your-metrc-doc.md" http://localhost:8080/metrcdocs/upload
```

### 2. Search Documents
```bash
curl "http://localhost:8080/metrcdocs/search?query=packages"
```

### 3. Use MCP Protocol
```bash
curl -X POST http://localhost:8080/api/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "tools/call",
    "params": {
      "name": "search_metrcdocs",
      "arguments": {
        "query": "package API endpoints"
      }
    }
  }'
```

## Benefits

1. **Privacy**: Keep sensitive METRC documentation local
2. **Performance**: Faster search through local files
3. **Customization**: Add your own METRC documentation
4. **Offline Support**: Works without external dependencies
5. **Fallback**: Still uses external sources when needed

## Troubleshooting

### Documents Not Found
1. Check that documents are in the correct folder: `uploads/metrcdocs/`
2. Ensure documents are in supported formats
3. Call the reindex endpoint: `POST /metrcdocs/reindex`

### Search Not Working
1. Verify the MCP server is running
2. Check the application logs for errors
3. Ensure documents contain the search terms

### Upload Issues
1. Check file permissions on the `uploads/metrcdocs/` folder
2. Ensure files are not corrupted
3. Verify file format is supported

## Future Enhancements

- PDF text extraction using Apache PDFBox
- Word document parsing using Apache POI
- Advanced search with fuzzy matching
- Document versioning
- Access control and user permissions
- Document categories and tagging
- Search result highlighting
- Document preview functionality
