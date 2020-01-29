from lib.session import Session, Credentials, APIError


def test_login(admin_session):
    try:
        admin_session.request2("POST", "/login")
        assert False, "Invalid login form did not raised exception"
    except APIError as err:
        assert err.response.status_code == 400
    resp = admin_session.request2("GET", "/users/current")
    assert resp.status_code == 200
    data = resp.json()
    assert set(data.keys()) == {"admin", "email", "id", "name", "username"}