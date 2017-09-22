#!/usr/bin/env python3
import argparse
import subprocess

if subprocess.call(['git', 'diff-index', '--quiet', 'HEAD']) != 0:
    raise RuntimeError('Working directory must be clean.')
parser = argparse.ArgumentParser()
parser.add_argument('version')
args = parser.parse_args()
subprocess.check_call([
    'mvn',
    'versions:set',
    '-DnewVersion={}'.format(args.version),
    '-DgenerateBackupPoms=false',
])
subprocess.check_call([
    'git',
    'commit',
    '-a',
    '-m', 'VERSION {}'.format(args.version),
])
subprocess.check_call([
    'git',
    'tag',
    '-a', 'v{}'.format(args.version),
    '-m', 'v{}'.format(args.version),
])
subprocess.check_call([
    'git',
    'push',
    '--tags',
])
