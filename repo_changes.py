#!/usr/bin/env python3
"""
Repository Change Report Generator

This script generates a Markdown report of changes between the local and remote repository.
It lists new commits and shows the exact changes (diffs) with statistics.
"""

import subprocess
import os
import datetime
import re
from pathlib import Path

def run_git_command(command, check_error=True):
    """Run a git command and return the output."""
    result = subprocess.run(
        command,
        shell=True,
        capture_output=True,
        text=True
    )
    if result.returncode != 0 and check_error:
        print(f"Error running command: {command}")
        print(f"Error message: {result.stderr}")
        return ""
    return result.stdout.strip()

def has_upstream():
    """Check if the current branch has an upstream branch configured."""
    result = subprocess.run(
        "git rev-parse --abbrev-ref @{u} 2>/dev/null",
        shell=True,
        capture_output=True,
        text=True
    )
    return result.returncode == 0

def get_repo_info():
    """Get basic repository information."""
    repo_name = run_git_command("git rev-parse --show-toplevel")
    repo_name = os.path.basename(repo_name)

    remote_url = run_git_command("git remote get-url origin", check_error=False)
    if not remote_url:
        remote_url = "No remote configured"

    current_branch = run_git_command("git branch --show-current")

    # Check if branch has upstream
    upstream_status = "Tracking remote branch" if has_upstream() else "No upstream configured"

    return {
        "name": repo_name,
        "remote_url": remote_url,
        "branch": current_branch,
        "upstream_status": upstream_status
    }

def get_unpushed_commits():
    """Get commits that haven't been pushed to remote."""
    # If there's no upstream, get all commits in the current branch
    if not has_upstream():
        commits_raw = run_git_command(
            "git log --pretty=format:'%h|%an|%ad|%s' --date=short -10"
        )
    else:
        # Fetch from remote to ensure we have the latest information
        run_git_command("git fetch")

        # Get commits that are in local but not in remote
        commits_raw = run_git_command(
            "git log @{u}..HEAD --pretty=format:'%h|%an|%ad|%s' --date=short"
        )

    if not commits_raw:
        return []

    commits = []
    for line in commits_raw.split('\n'):
        if line:
            hash_val, author, date, subject = line.split('|', 3)
            commits.append({
                "hash": hash_val,
                "author": author,
                "date": date,
                "subject": subject
            })

    return commits

def get_changes():
    """Get the exact changes (diffs) between local and remote."""
    if not has_upstream():
        # If no upstream, show changes in the last commit or uncommitted changes
        staged_changes = run_git_command("git diff --staged")
        unstaged_changes = run_git_command("git diff")
        last_commit_changes = run_git_command("git diff HEAD~1 HEAD")

        # Combine all changes
        changes = []
        if staged_changes:
            changes.append(("Staged Changes", staged_changes))
        if unstaged_changes:
            changes.append(("Unstaged Changes", unstaged_changes))
        if not (staged_changes or unstaged_changes) and last_commit_changes:
            changes.append(("Last Commit Changes", last_commit_changes))
    else:
        run_git_command("git fetch")
        # Get changes compared to upstream
        changes = [("Changes vs Upstream", run_git_command("git diff @{u} HEAD"))]

    return changes

def get_change_stats():
    """Get statistics about changes between local and remote."""
    if not has_upstream():
        # If no upstream, show stats for the last commit or uncommitted changes
        stats = {}

        # Check for staged or unstaged changes
        staged_stats = run_git_command("git diff --shortstat --staged")
        unstaged_stats = run_git_command("git diff --shortstat")

        if staged_stats or unstaged_stats:
            stats["details"] = "Staged changes: " + staged_stats if staged_stats else "No staged changes"
            if unstaged_stats:
                stats["details"] += "\nUnstaged changes: " + unstaged_stats
        else:
            # If no working tree changes, show stats for the last commit
            last_commit_stats = run_git_command("git diff --shortstat HEAD~1 HEAD")
            stats["details"] = "Last commit: " + last_commit_stats if last_commit_stats else "No changes in last commit"

        return stats
    else:
        run_git_command("git fetch")

        # Get line changes compared to upstream
        insertions_deletions = run_git_command("git diff --shortstat @{u} HEAD")

        return {
            "details": insertions_deletions if insertions_deletions else "No changes"
        }

def generate_markdown_report(repo_info, commits, changes, stats):
    """Generate a Markdown report of the changes."""
    now = datetime.datetime.now()
    date_time = now.strftime("%Y-%m-%d %H:%M:%S")

    md_content = [
        f"# Repository Change Report\n",
        f"Generated on: {date_time}\n",
        f"## Repository Information\n",
        f"- **Repository:** {repo_info['name']}",
        f"- **Remote URL:** {repo_info['remote_url']}",
        f"- **Branch:** {repo_info['branch']}",
        f"- **Status:** {repo_info['upstream_status']}\n",
    ]

    # Add commit information
    if not has_upstream():
        md_content.append(f"## Latest Commits\n")
    else:
        md_content.append(f"## Unpushed Commits\n")

    if commits:
        for commit in commits:
            md_content.append(
                f"- **{commit['hash']}** - {commit['date']} - {commit['author']}\n  "
                f"{commit['subject']}"
            )
    else:
        md_content.append("No commits found.")

    md_content.append("\n## Changes\n")

    if changes:
        for change_type, diff in changes:
            if diff:
                md_content.append(f"### {change_type}\n")
                md_content.append("```diff")
                md_content.append(diff)
                md_content.append("```\n")
    else:
        md_content.append("No changes found.")

    # Add statistics
    md_content.append(f"## Change Statistics\n")
    md_content.append(f"{stats.get('details', 'No statistics available')}\n")

    return "\n".join(md_content)

def main():
    """Main function to run the script."""
    # Check if we're in a git repository
    if not run_git_command("git rev-parse --is-inside-work-tree"):
        print("Error: This is not a git repository.")
        return

    # Get information
    repo_info = get_repo_info()
    commits = get_unpushed_commits()
    changes = get_changes()
    stats = get_change_stats()

    # Generate report
    md_content = generate_markdown_report(repo_info, commits, changes, stats)

    # Determine the output file path
    output_file = Path(f"{repo_info['name']}_changes_{datetime.datetime.now().strftime('%Y%m%d_%H%M%S')}.md")

    # Write the report to file
    with open(output_file, 'w') as f:
        f.write(md_content)

    print(f"Change report generated: {output_file}")

if __name__ == "__main__":
    main()