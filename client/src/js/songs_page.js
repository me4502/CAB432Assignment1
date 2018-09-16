import {API_URL} from "./util";

export function populateSongs() {
    const children = document.querySelector("#song-container");

    while (children.hasChildNodes()) {
        children.removeChild(children.lastChild);
    }

    const search = document.querySelector("#search_bar").value;

    window.fetch(API_URL + '/track/search/' + search)
        .then(res => res.json())
        .then(json => {
            for (let song in json) {
                song = json[song];
                const div = `<div class="col s12 m4">
                      <div class="card">
                        <span class="card-title">
                          ${song['song']} by ${song['artist']}
                        </span>
                        <div class="card-action">
                          <a href="#" onclick="main.moveToLabelPage('${song['id']}', '${song['song']}', '${song['artist']}')">Choose</a>
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

export function submitSongSearchBar(e) {
    if (e.which === 13) { // Enter Key (There has to be some constant right?)
        populateSongs();
        e.preventDefault();
    }
}

export function moveToLabelPage(trackId, song, artist) {
    localStorage.setItem("chosenTrack", trackId);
    localStorage.setItem("chosenTrackName", song);
    localStorage.setItem("chosenTrackArtist", artist);
    window.location.href = "/select_labels";
}