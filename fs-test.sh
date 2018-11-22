#!/bin/bash

failed() {
  eval $1 && echo "TEST: Failed" || echo "TEST: OK"
}

ok() {
  eval $1 && echo "TEST: OK" || echo "TEST: Failed"
}

# Test tree
fs_root_dir=`pwd`
ok "mkdir 123"
ok "cd 123"
ok "mkdir 456"
ok "cd 456"
ok "mkdir 789"
ok "cd 789"
ok "mkdir 555"
failed "rm 555"
ok "rmdir 555"
ok "mkdir 777"
ok "rm -r 777"

cd $fs_root_dir

# Test rm
failed "mkdir 123"
ok "mkdir 888"
failed "rm 888"
ok "rm -r 888"

# Test rename 1
ok "mkdir oldName"
ok "mv oldName newName"
ok "ls newName"

# Test rename 2
ok "mv newName 123/nn"
ok "ls 123/nn"
failed "ls newName"

# Test rename 3
failed "mv 123/nn lol/kek"
failed "mv 123/nn notExistsPath/ololo"
ok "mv 123/nn 123/456/789/AAA"

# Test recursive rmdir
ok "mkdir 123/456/789/AAA/BBB"
failed "rm 123/456/789/AAA"
ok "ls 123/456/789/AAA/BBB"
failed "mkdir 123/456/789/AAA/BBB"
ok "rm -rf 123/456/789/AAA"
failed "ls 123/456/789/AAA"

# Test creating dir in non-existent dir
failed "mkdir no/no"
failed "ls no/no"

# Test file creating
ok "touch 1"
ok "touch 123/sub"
failed "touch 124/sub"

# Test file operations
ok "cp 1 2"
ok "mv 2 333"
failed "rm 2"
failed "ls 2"
ok "ls 1"
ok "ls 333"
ok "rm 1"

# Test working with file content
ok "echo 'cat' > 4"
ok "echo 'new' >> 4"
ok "echo '' > 4"
ok "cat 4"
failed "cat 2"

# Test file moving into non-existent dir
failed "mv 4 ./1/"
failed "cp 4 ./1/"
ok "mv 4 ./123"
ok "cp 333 ./123"

# Test file removing
ok "rm 333"
failed "rm 123"
ok "rm -r 123"

# Test moving to existing
ok "echo > test11_1"
ok "cat test11_1"
ok "echo > test11_2"
ok "cat test11_2"
failed "mv test11_1 test11_2"
