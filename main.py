import os
import hashlib
from tkinter import filedialog, messagebox, Tk

def get_file_hash(file_path):
    """Generate MD5 hash for a file."""
    hash_md5 = hashlib.md5()
    with open(file_path, 'rb') as f:
        for chunk in iter(lambda: f.read(8192), b""):
            hash_md5.update(chunk)
    return hash_md5.hexdigest()

def walk(folder, unique_files, duplicates):
    """Walk through the folder and find duplicates based on file hash."""
    for root, dirs, files in os.walk(folder):
        for file in files:
            file_path = os.path.join(root, file)
            try:
                file_hash = get_file_hash(file_path)
                if file_hash in unique_files:
                    duplicates.setdefault(file_hash, []).append(file_path)
                else:
                    unique_files[file_hash] = file_path
            except (IOError, hashlib.HashlibError) as e:
                print(f"Error processing file {file_path}: {e}")

def delete_duplicates(folder):
    """Find and delete duplicate files in the given folder."""
    unique_files = {}
    duplicates = {}

    walk(folder, unique_files, duplicates)

    if not duplicates:
        messagebox.showinfo("No Duplicates", "No duplicate files found in the selected directory")
        return

    message = f"{len(duplicates)} sets of duplicate files found:\n\n"
    for files in duplicates.values():
        for file in files:
            message += f"{file}\n"
        message += "\n"
    message += "Do you want to delete all duplicates?"

    answer = messagebox.askyesno("Duplicate files found", message)
    if answer:
        for files in duplicates.values():
            for file in files:
                os.remove(file)
        messagebox.showinfo("Deleted", "All duplicate files have been deleted")
    else:
        messagebox.showinfo("No Deletion", "No files were deleted")

def main():
    """Main function to run the duplicate file finder."""
    root = Tk()
    root.withdraw()  # Hide the root window
    folder = filedialog.askdirectory(title="Select Folder")
    if folder:
        delete_duplicates(folder)

if __name__ == "__main__":
    main()
