import 'whatwg-fetch'
import 'babel-polyfill'

const API_URL = 'http://localhost:5078';

export function moveToTagPage(imageId, url) {
    localStorage.setItem("chosenImage", imageId);
    localStorage.setItem("chosenImageURL", url);
    window.location.href = "/select_tags";
}

export function moveToExportPage() {
    const chips = document.querySelector(".chips");
    const tags = [];

    let inst = M.Chips.getInstance(chips);
    for (let i in inst.chipsData) {
        tags.push(inst.chipsData[i]['tag']);
    }

    localStorage.setItem("chosenTags", tags);
    window.location.href = "/export";
}

export function populateImages() {
    const children = document.querySelector("#image-container");

    while (children.hasChildNodes()) {
        children.removeChild(children.lastChild);
    }

    const search = document.querySelector("#search_bar").value;

    window.fetch(API_URL + '/image/search/' + search)
        .then(res => res.json())
        .then(json => {
            for (let url in json) {
                url = json[url];
                const div = `<div class="col s12 m4">
                      <div class="card">
                        <div class="card-image">
                          <img src="${url['url']}">
                        </div>
                        <div class="card-action">
                          <a href="#" onclick="main.moveToTagPage('${url['id']}', '${url['url']}')">Choose</a>
                        </div>
                      </div>
                    </div>`;
                let child = document.createElement('div');
                child.innerHTML = div;
                children.appendChild(child);
            }
        }).catch(ex => {
            children.innerHTML = "<p>Failed to search: " + ex.message +"</p>";
            console.log('Failed to parse', ex)
        });
}

export function loadTags() {
    if (checkForStorage('chosenImage') && checkForStorage('chosenImageURL')) {
        const children = document.querySelector("#tag-container");
        const confirmButton = document.querySelector("#confirm-button");

        let image = localStorage.getItem('chosenImage');
        Promise.all([
            window.fetch(API_URL + '/tag/popular').then(r => r.json()),
            window.fetch(API_URL + '/image/tag/' + image).then(r => r.json())
        ]).then(jsonTuple => {
            let popularTags = jsonTuple[0];
            let foundTags = jsonTuple[1];
            let tags = [];
            for (let tag in foundTags) {
                tag = foundTags[tag];
                tags.push({'tag': tag});
            }
            let autoTags = {};
            for (let autoTag in popularTags) {
                autoTag = popularTags[autoTag];
                autoTags[autoTag] = null;
            }

            while (children.hasChildNodes()) {
                children.removeChild(children.lastChild);
            }
            children.innerHTML = "<div class=\"chips chips-tags\"></div>";
            const chips = document.querySelector("#tag-container");
            const instances = M.Chips.init(chips, {
                data: tags,
                autocompleteOptions: {
                    data: autoTags,
                    limit: Infinity,
                    minLength: 1
                }
            });

            confirmButton.style.display = "";
        }).catch(ex => {
            while (children.hasChildNodes()) {
                children.removeChild(children.lastChild);
            }
            children.innerHTML = "<p>Failed to load tags: " + ex.message +"</p>";
            console.log('Failed to parse', ex)
        });
    }
}

export function loadExport() {
    if (checkForStorage('chosenImageURL') && checkForStorage('chosenTags')) {
        const songTag = document.querySelector("#song-name");

        const children = document.querySelector("#canvas-container");

        let imageUrl = localStorage.getItem('chosenImageURL');
        let tags = localStorage.getItem('chosenTags');

        window.fetch(API_URL + '/track/get/' + tags)
            .then(r => r.json())
            .then(json => {
                songTag.innerHTML = "Song Name: " + json['track'] + " by " + json['artist'];
                window.fetch(API_URL + '/track/lyrics/' + json['track'] + '/' + json['artist'])
                    .then(r => r.json())
                    .then(json => {
                        let canvas = document.querySelector("#export-canvas");
                        let context = canvas.getContext("2d");
                        let imageObj = new Image();
                        imageObj.onload = () => {
                            canvas.width = imageObj.width;
                            canvas.height = imageObj.height;

                            let lyricsArray = json['lyrics']
                                .split('\n')
                                .filter(line => line.length > 0)
                                .slice(0, 4);

                            let longestLyric = Math.max(...lyricsArray.map(line => line.length));

                            context.drawImage(imageObj, 0, 0);
                            context.font = "32pt AngeliqueRose, Cursive";
                            if (longestLyric > 48) {
                                context.font = "28pt AngeliqueRose, Cursive";
                            }
                            context.textAlign = "center";
                            context.strokeStyle = 'black';
                            context.fillStyle = 'white';
                            context.lineWidth = 4;

                            let fontHeight = 48;
                            let fontSize = fontHeight * (lyricsArray.length - 1);

                            let x = canvas.width / 2;
                            let fontOffset = (canvas.height / 2) - (fontSize / 2);

                            for (let line in lyricsArray) {
                                let y = fontOffset + fontHeight * line;
                                let text = lyricsArray[line];

                                context.strokeText(text, x, y);
                                context.fillText(text, x, y);
                            }

                            // Show the download button
                            let downloadButton = document.querySelector("#download");
                            downloadButton.style.display = "";
                        };
                        imageObj.onerror = err => {
                              songTag.innerHTML += "   Failed to load image!";
                              console.log(err);
                        };
                        imageObj.src = imageUrl;
                        imageObj.crossOrigin = "Anonymous";
                    }).catch(ex => {
                        while (children.hasChildNodes()) {
                            children.removeChild(children.lastChild);
                        }
                        children.innerHTML = "<p>Failed to load lyrics: " + ex.message +"</p>";
                        console.log('Failed to load lyrics', ex)
                    });
            }).catch(ex => {
                while (children.hasChildNodes()) {
                    children.removeChild(children.lastChild);
                }
                children.innerHTML = "<p>Failed to load song: " + ex.message +"</p>";
                console.log('Failed to load song', ex)
            });
    }
}

export function downloadCanvas() {
    let canvas = document.querySelector("#export-canvas");
    let downloadButton = document.querySelector("#download");

    let image = canvas.toDataURL("image/png")
        .replace("image/png", "image/octet-stream");
    downloadButton.setAttribute("href", image);
}

export function submitSearchBar(e) {
    if (e.which === 13) { // Enter Key (There has to be some constant right?)
        populateImages();
        e.preventDefault();
    }
}

export function checkForStorage(key) {
    if (localStorage.getItem(key) === null) {
        window.location.href = "/";
        return false;
    }
    return true;
}