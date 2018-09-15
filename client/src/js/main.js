import 'whatwg-fetch'
import 'babel-polyfill'

const API_URL = 'http://localhost:5078';

export function moveToTagPage(imageId) {
    localStorage.setItem("chosenImage", imageId);
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
                          <a href="#" onclick="main.moveToTagPage('${url['id']}')">Choose</a>
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
    if (checkForStorage('chosenImage')) {
        const children = document.querySelector("#tag-container");

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
    if (checkForStorage('chosenImage') && checkForStorage('chosenTags')) {
        const children = document.querySelector("#tag-container");
        const songTag = document.querySelector("#song-name");

        let image = localStorage.getItem('chosenImage');
        let tags = localStorage.getItem('chosenTags');

        window.fetch(API_URL + '/track/get/' + tags)
            .then(r => r.json())
            .then(json => {
                songTag.innerHTML = "Song Name: " + json['track'] + " by " + json['artist'];
                window.fetch(API_URL + '/track/lyrics/' + json['track'] + '/' + json['artist'])
                    .then(r => r.json())
                    .then(json => {
                        console.log(json);
                    });
            })
    }
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