import {API_URL, checkForStorage} from "./util";

function createImage(imageObj, lyrics) {
    let canvas = document.querySelector("#export-canvas");
    let context = canvas.getContext("2d");

    canvas.width = imageObj.width;
    canvas.height = imageObj.height;

    let lyricsArray = lyrics
        .split('\n')
        .filter(line => line.length > 0)
        .slice(0, 4);

    let longestLyric = Math.max(...lyricsArray.map(line => line.length));

    context.drawImage(imageObj, 0, 0);
    context.font = "30pt AngeliqueRose, Cursive";
    if (longestLyric * 7 > canvas.width) {
        context.font = "22pt AngeliqueRose, Cursive";
    } else if (longestLyric * 10 > canvas.width) {
        context.font = "26pt AngeliqueRose, Cursive";
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

    let makeAnotherButton = document.querySelector("#make-another");
    makeAnotherButton.style.display = "";
}

function songExport() {
    if (checkForStorage('chosenTrack') && checkForStorage('chosenTrackName')
        && checkForStorage('chosenTrackArtist') && checkForStorage('chosenLabels')) {
        const songTag = document.querySelector("#song-name");

        const children = document.querySelector("#canvas-container");

        let labels = localStorage.getItem('chosenLabels');

        let trackId = localStorage.getItem('chosenTrack');
        let songName = localStorage.getItem('chosenTrackName');
        let songArtist = localStorage.getItem('chosenTrackArtist');

        window.fetch(API_URL + '/image/search_single/' + labels)
            .then(r => r.json())
            .then(imageJson => {
                songTag.innerHTML = "Song Name: " + songName + " by " + songArtist;
                window.fetch(API_URL + '/track/lyrics/' + trackId)
                    .then(r => r.json())
                    .then(json => {
                        let imageObj = new Image();
                        imageObj.onload = () => {
                            createImage(imageObj, json['lyrics']);
                        };
                        imageObj.onerror = err => {
                            songTag.innerHTML += "   Failed to load image!";
                            console.log(err);
                        };
                        imageObj.src = imageJson['url'];
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

function imageExport() {
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
                        let imageObj = new Image();
                        imageObj.onload = () => {
                            createImage(imageObj, json['lyrics']);
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

export function loadExport() {
    if (checkForStorage('exportMode')) {
        let exportMode = localStorage.getItem('exportMode');
        if (exportMode === 'song') {
            songExport();
        } else if (exportMode === 'image') {
            imageExport();
        }
    }
}

export function downloadCanvas() {
    let canvas = document.querySelector("#export-canvas");
    let downloadButton = document.querySelector("#download");

    let image = canvas.toDataURL("image/png")
        .replace("image/png", "image/octet-stream");
    downloadButton.setAttribute("href", image);
}

export function makeAnother() {
    if (checkForStorage('exportMode')) {
        let exportMode = localStorage.getItem('exportMode');
        if (exportMode === 'song') {
            window.location.href = "/by_song";
        } else if (exportMode === 'image') {
            window.location.href = "/";
        }
    }
}