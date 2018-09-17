import {API_URL, checkForStorage} from "./util";

export function loadLabels() {
    if (checkForStorage('chosenTrack') && checkForStorage('chosenTrackName') && checkForStorage('chosenTrackArtist')) {
        const children = document.querySelector("#label-container");
        const confirmButton = document.querySelector("#confirm-button");

        let song = localStorage.getItem('chosenTrackName');
        let artist = localStorage.getItem('chosenTrackArtist');
        window.fetch(API_URL + '/label/get/' + song + '/' + artist)
            .then(r => r.json())
            .then(labelsJson => {
                let labels = [];
                for (let label in labelsJson) {
                    label = labelsJson[label];
                    labels.push({'tag': label});
                }

                if (labels.length === 0) {
                    labels.push({'tag': 'countryside'})
                }

                while (children.hasChildNodes()) {
                    children.removeChild(children.lastChild);
                }
                children.innerHTML = "<div class=\"chips\"></div>";
                const chips = document.querySelector(".chips");
                M.Chips.init(chips, {
                    data: labels,
                    limit: 20,
                });

                confirmButton.style.display = "";
            }).catch(ex => {
                while (children.hasChildNodes()) {
                    children.removeChild(children.lastChild);
                }
                children.innerHTML = "<p>Failed to load labels: " + ex.message +"</p>";
                console.log('Failed to parse', ex)
            });
    }
}

export function moveToSongExportPage() {
    const chips = document.querySelector(".chips");
    const labels = [];

    let inst = M.Chips.getInstance(chips);
    for (let i in inst.chipsData) {
        labels.push(inst.chipsData[i]['tag']);
    }

    if (labels.length === 0) {
        M.toast({html: 'You must select at least one label!'})
    } else {
        localStorage.setItem("chosenLabels", labels);
        localStorage.setItem("exportMode", "song");
        window.location.href = "/export";
    }
}
