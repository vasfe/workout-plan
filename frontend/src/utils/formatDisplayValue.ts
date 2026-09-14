export default function formatDisplayValue(value: string) {
    return value
        .toLowerCase()
        .replace(/_/g, " ")
        .replace(/\s+/g, " ")
        .replace(/\b\w/g, (char) => char.toUpperCase())
        .trim();
}
