export const API_URL = '';

export function checkForStorage(key) {
    if (localStorage.getItem(key) === null) {
        window.location.href = "/";
        return false;
    }
    return true;
}