<!DOCTYPE html>
<html lang="en">
    <#include "header.ftl">
<body>
    <#include "nav.ftl">

<div class="container">
    <div class="row">
        <h3>Finalise The Image</h3>
    </div>

    <div class="row">
        <p id="song-name">Song Name: Loading...</p>
    </div>

    <canvas id="export-canvas">
        Your browser does not support Canvas, sorry.
    </canvas>

    <div class="row">
        <a id="download" style="display: none;" download="export.png">
            <button type="button" class="btn waves-effect waves-light" onClick="main.downloadCanvas()">Download</button>
        </a>
    </div>
</div>

    <#include "js.ftl">
    <script>
        main.loadExport();
    </script>
</body>
</html>