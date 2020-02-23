from lib.session import Session, Credentials, APIError


def test_headers():
    session = Session(None)
    resp = session.request_raw("POST", "/login")
    print(resp.text)
    assert resp.status_code == 406


def test_login(admin_session: Session):
    try:
        admin_session.login({})
        assert False, "Invalid login form did not raised exception"
    except APIError as err:
        assert err.response.status_code == 400
    resp = admin_session.users.current
    assert resp


def test_user_list(admin_session: Session):
    assert admin_session.users.list()


def test_create_delete_user(admin_session: Session):
    admin_session.request2("POST", "/users", json={
        "user": dict(username="test", email="test@localhost", name="", admin=False),
        "password": "123"
    })
    user = admin_session.users.by_username("test")
    print(user)
    assert Session(Credentials("test", "123"))
    assert user
    assert user.delete()
