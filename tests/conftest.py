import sys
from pathlib import Path

sys.path.append(Path(__file__).parent)

import pytest

from lib.session import Credentials, Session


def pytest_addoption(parser):
    parser.addoption("--username", help="ar2 user name", required=True)
    parser.addoption("--password", help="ar2 user password", required=True)


@pytest.fixture
def admin_creds(pytestconfig):
    return Credentials(
        pytestconfig.getoption("username"), pytestconfig.getoption("password")
    )


@pytest.fixture
def admin_session(admin_creds):
    sess = Session(admin_creds)
    return sess
